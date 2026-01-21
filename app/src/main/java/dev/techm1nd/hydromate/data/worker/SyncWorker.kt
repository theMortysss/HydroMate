package dev.techm1nd.hydromate.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.techm1nd.hydromate.domain.repositories.SyncRepository
import java.util.concurrent.TimeUnit

/**
 * Background worker для периодической синхронизации данных
 * Работает только для зарегистрированных пользователей
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // CRITICAL: Check if user is authenticated and NOT anonymous
            val currentUser = firebaseAuth.currentUser

            if (currentUser == null) {
                android.util.Log.d(TAG, "No user logged in, skipping sync")
                return Result.success()
            }

            if (currentUser.isAnonymous) {
                android.util.Log.d(TAG, "Anonymous user, skipping sync")
                return Result.success()
            }

            // Синхронизировать все данные только для зарегистрированных пользователей
            syncRepository.syncAll()
                .onSuccess {
                    android.util.Log.d(TAG, "Background sync completed successfully")
                }
                .onFailure { exception ->
                    android.util.Log.e(TAG, "Background sync failed", exception)
                    // Retry only on non-auth errors
                    return if (exception.message?.contains("auth", ignoreCase = true) == true) {
                        Result.failure()
                    } else {
                        Result.retry()
                    }
                }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Background sync exception", e)

            // Don't retry on critical errors
            if (e is SecurityException || e is IllegalStateException) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val WORK_NAME = "sync_work"

        /**
         * Запланировать периодическую синхронизацию
         * Вызывается только когда пользователь залогинен (не анонимно)
         */
        fun schedule(context: Context) {
            // Check if user is logged in and not anonymous before scheduling
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null || currentUser.isAnonymous) {
                android.util.Log.d(TAG, "Not scheduling sync: user is null or anonymous")
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true) // Don't drain battery
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 1, // Allow 1 hour flex
                flexTimeIntervalUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Don't restart if already scheduled
                syncRequest
            )

            android.util.Log.d(TAG, "Sync worker scheduled for registered user")
        }

        /**
         * Отменить синхронизацию
         * Вызывается при выходе или переходе на анонимный аккаунт
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            android.util.Log.d(TAG, "Sync worker cancelled")
        }

        /**
         * Проверить, запланирован ли worker
         */
        fun isScheduled(context: Context): Boolean {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()
            return workInfos.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
        }
    }
}