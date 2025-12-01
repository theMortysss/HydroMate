package sdf.bitt.hydromate.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var getUserSettingsUseCase: GetUserSettingsUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Восстанавливаем расписание уведомлений после перезагрузки
            scope.launch {
                try {
                    val settings = getUserSettingsUseCase().first()
                    if (settings.notificationsEnabled) {
                        notificationScheduler.scheduleNotifications(settings)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}