package dev.techm1nd.hydromate.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.usecases.setting.GetUserSettingsUseCase
import javax.inject.Inject

@AndroidEntryPoint
class SnoozeActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getUserSettingsUseCase: GetUserSettingsUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var notificationManager: HydroMateNotificationManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == HydroMateNotificationManager.ACTION_SNOOZE) {
            val snoozeMinutes = intent.getIntExtra(
                HydroMateNotificationManager.EXTRA_SNOOZE_MINUTES,
                10
            )

            scope.launch {
                try {
                    val settings = getUserSettingsUseCase().first()

                    // Отменяем текущее уведомление
                    notificationManager.cancelReminderNotifications()

                    // Планируем отложенное напоминание
                    notificationScheduler.scheduleSnoozeReminder(settings, snoozeMinutes)

                    // Показываем Toast пользователю
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Reminder snoozed for $snoozeMinutes minutes ⏰",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}