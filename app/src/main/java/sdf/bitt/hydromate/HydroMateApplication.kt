package sdf.bitt.hydromate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.usecases.InitializeDefaultDrinksUseCase
import sdf.bitt.hydromate.domain.usecases.achievement.InitializeAchievementsUseCase
import javax.inject.Inject

@HiltAndroidApp
class HydroMateApplication : Application() {

    @Inject
    lateinit var initializeDefaultDrinksUseCase: InitializeDefaultDrinksUseCase

    @Inject
    lateinit var initializeAchievementsUseCase: InitializeAchievementsUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Инициализация дефолтных напитков при первом запуске
        applicationScope.launch {
            initializeDefaultDrinksUseCase()
                .onSuccess {
                    android.util.Log.d("HydroMate", "Default drinks initialized successfully")
                }
                .onFailure { error ->
                    android.util.Log.e("HydroMate", "Failed to initialize default drinks", error)
                }
        }

        // NEW: Инициализация достижений при первом запуске
        applicationScope.launch {
            initializeAchievementsUseCase()
                .onSuccess {
                    android.util.Log.d("HydroMate", "Achievements initialized successfully")
                }
                .onFailure { error ->
                    android.util.Log.e("HydroMate", "Failed to initialize achievements", error)
                }
        }

        android.util.Log.d("HydroMate", "Application initialized")
    }
}