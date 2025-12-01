package sdf.bitt.hydromate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import sdf.bitt.hydromate.domain.usecases.InitializeDefaultDrinksUseCase
import sdf.bitt.hydromate.ui.notification.NotificationScheduler
import javax.inject.Inject

@HiltAndroidApp
class HydroMateApplication : Application() {

    @Inject
    lateinit var initializeDefaultDrinksUseCase: InitializeDefaultDrinksUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var getUserSettingsUseCase: GetUserSettingsUseCase

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

        // Инициализация системы уведомлений
        applicationScope.launch {
            try {
                val settings = getUserSettingsUseCase().first()
                if (settings.notificationsEnabled) {
                    notificationScheduler.scheduleNotifications(settings)
                    android.util.Log.d("HydroMate", "Notifications scheduled successfully")
                }
            } catch (e: Exception) {
                android.util.Log.e("HydroMate", "Failed to schedule notifications", e)
            }
        }
    }
}