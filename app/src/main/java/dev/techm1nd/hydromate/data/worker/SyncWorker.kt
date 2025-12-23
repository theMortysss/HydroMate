package dev.techm1nd.hydromate.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.techm1nd.hydromate.domain.repositories.SyncRepository
import java.util.concurrent.TimeUnit

/**
 * Background worker для периодической синхронизации данных
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Синхронизировать все данные
            syncRepository.syncAll()
                .onSuccess {
                    android.util.Log.d(TAG, "Background sync completed successfully")
                }
                .onFailure { exception ->
                    android.util.Log.e(TAG, "Background sync failed", exception)
                    return Result.retry()
                }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Background sync exception", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val WORK_NAME = "sync_work"

        /**
         * Запланировать периодическую синхронизацию
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        /**
         * Отменить синхронизацию
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}