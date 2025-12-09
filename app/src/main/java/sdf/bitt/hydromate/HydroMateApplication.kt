package sdf.bitt.hydromate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.usecases.InitializeDefaultDrinksUseCase
import javax.inject.Inject

@HiltAndroidApp
class HydroMateApplication : Application() {

    @Inject
    lateinit var initializeDefaultDrinksUseCase: InitializeDefaultDrinksUseCase

    // Убрали notificationScheduler и getUserSettingsUseCase из Application
    // Инициализация уведомлений теперь происходит только при необходимости

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

        // REMOVED: Инициализация уведомлений
        // Теперь уведомления инициализируются только в двух случаях:
        // 1. После перезагрузки устройства (BootCompletedReceiver)
        // 2. При изменении настроек (через NotificationScheduler.scheduleNotifications)

        android.util.Log.d("HydroMate", "Application initialized")
    }
}