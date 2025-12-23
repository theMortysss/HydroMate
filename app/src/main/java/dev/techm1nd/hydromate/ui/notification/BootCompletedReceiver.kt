package dev.techm1nd.hydromate.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.usecases.GetUserSettingsUseCase
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
            Log.d(TAG, "Boot completed, restoring notifications")

            // Восстанавливаем расписание уведомлений после перезагрузки
            scope.launch {
                try {
                    val settings = getUserSettingsUseCase().first()

                    if (settings.notificationsEnabled) {
                        // Восстанавливаем основное расписание
                        notificationScheduler.scheduleNotifications(settings)

                        // Восстанавливаем отложенное напоминание если было
                        notificationScheduler.restoreSnoozeReminderIfNeeded(settings)

                        Log.d(TAG, "Notifications restored successfully")
                    } else {
                        Log.d(TAG, "Notifications disabled, skipping restore")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore notifications", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}