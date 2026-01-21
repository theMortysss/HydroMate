package dev.techm1nd.hydromate

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.data.worker.SyncWorker
import dev.techm1nd.hydromate.domain.usecases.hydration.InitializeDefaultDrinksUseCase
import dev.techm1nd.hydromate.domain.usecases.achievement.InitializeAchievementsUseCase
import javax.inject.Inject

@HiltAndroidApp
class HydroMateApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var initializeDefaultDrinksUseCase: InitializeDefaultDrinksUseCase

    @Inject
    lateinit var initializeAchievementsUseCase: InitializeAchievementsUseCase

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Инициализация дефолтных напитков при первом запуске (работает offline)
        applicationScope.launch {
            initializeDefaultDrinksUseCase()
                .onSuccess {
                    android.util.Log.d("HydroMate", "Default drinks initialized successfully")
                }
                .onFailure { error ->
                    android.util.Log.e("HydroMate", "Failed to initialize default drinks", error)
                }
        }

        // Инициализация достижений при первом запуске (работает offline)
        applicationScope.launch {
            initializeAchievementsUseCase()
                .onSuccess {
                    android.util.Log.d("HydroMate", "Achievements initialized successfully")
                }
                .onFailure { error ->
                    android.util.Log.e("HydroMate", "Failed to initialize achievements", error)
                }
        }

        // FIXED: Schedule sync ONLY if user is authenticated and NOT anonymous
        // Don't schedule on first launch - will be scheduled after successful login/linking
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            if (!SyncWorker.isScheduled(this)) {
                SyncWorker.schedule(this)
                android.util.Log.d("HydroMate", "Sync worker scheduled for registered user")
            } else {
                android.util.Log.d("HydroMate", "Sync worker already scheduled")
            }
        } else {
            android.util.Log.d("HydroMate", "User is null or anonymous, sync not scheduled")
        }

        android.util.Log.d("HydroMate", "Application initialized")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}