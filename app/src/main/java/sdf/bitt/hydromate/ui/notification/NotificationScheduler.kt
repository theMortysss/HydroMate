package sdf.bitt.hydromate.ui.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import sdf.bitt.hydromate.domain.entities.UserSettings
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNotifications(settings: UserSettings) {
        if (!settings.notificationsEnabled) {
            cancelNotifications()
            return
        }

        // Отменяем предыдущие уведомления
        cancelNotifications()

        // Планируем новые уведомления
        val now = LocalDateTime.now()
        val currentTime = now.toLocalTime()

        // Проверяем, находимся ли мы в активном периоде
        if (!isInActiveTime(currentTime, settings.wakeUpTime, settings.bedTime)) {
            // Если сейчас не активное время, планируем первое уведомление на время пробуждения
            Log.d(TAG, "Outside active time, scheduling for wake up time")
            scheduleNextReminder(settings, settings.wakeUpTime)
            return
        }

        // Планируем следующее уведомление с учетом интервала
        val nextReminderTime = calculateNextReminderTime(currentTime, settings)
        Log.d(TAG, "Scheduling next reminder for: $nextReminderTime")
        scheduleNextReminder(settings, nextReminderTime)
    }

    /**
     * Планирует следующее напоминание на утро следующего дня (когда цель достигнута)
     */
    fun scheduleNextDayReminder(settings: UserSettings) {
        if (!settings.notificationsEnabled) {
            cancelNotifications()
            return
        }

        // Отменяем текущие напоминания
        cancelNotifications()

        // Планируем на завтра в время пробуждения
        val tomorrow = LocalDateTime.now().plusDays(1)
        val tomorrowWakeTime = tomorrow.toLocalDate().atTime(settings.wakeUpTime)

        Log.d(TAG, "Goal reached! Scheduling next reminder for tomorrow: $tomorrowWakeTime")
        scheduleReminderAt(tomorrowWakeTime)
    }

    private fun scheduleNextReminder(settings: UserSettings, time: LocalTime) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Если время уже прошло сегодня, планируем на завтра
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        scheduleAlarm(calendar.timeInMillis)
    }

    private fun scheduleReminderAt(dateTime: LocalDateTime) {
        val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        scheduleAlarm(millis)
    }

    private fun scheduleAlarm(triggerAtMillis: Long) {
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = WaterReminderReceiver.ACTION_WATER_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Exact alarm scheduled for: ${Date(triggerAtMillis)}")
                } else {
                    // Fallback для случая, когда нет разрешения на точные будильники
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Inexact alarm scheduled for: ${Date(triggerAtMillis)}")
                }
            } else {
                // Android 11 и ниже
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm scheduled for: ${Date(triggerAtMillis)}")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    fun cancelNotifications() {
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = WaterReminderReceiver.ACTION_WATER_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Notifications cancelled")
        }
    }

    private fun calculateNextReminderTime(
        currentTime: LocalTime,
        settings: UserSettings
    ): LocalTime {
        val intervalMinutes = settings.notificationInterval.toLong()
        var nextTime = currentTime.plusMinutes(intervalMinutes)

        // Если следующее время выходит за время сна, планируем на время пробуждения следующего дня
        if (nextTime.isAfter(settings.bedTime)) {
            nextTime = settings.wakeUpTime
        }

        return nextTime
    }

    private fun isInActiveTime(
        currentTime: LocalTime,
        wakeUpTime: LocalTime,
        bedTime: LocalTime
    ): Boolean {
        return if (wakeUpTime.isBefore(bedTime)) {
            // Обычный случай (например, 08:00 - 22:00)
            currentTime.isAfter(wakeUpTime) && currentTime.isBefore(bedTime)
        } else {
            // Случай через полночь (например, 22:00 - 08:00)
            currentTime.isAfter(wakeUpTime) || currentTime.isBefore(bedTime)
        }
    }

    companion object {
        private const val REQUEST_CODE = 1000
        private const val TAG = "NotificationScheduler"
    }
}