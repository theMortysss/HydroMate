package dev.techm1nd.hydromate.ui.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.techm1nd.hydromate.domain.entities.NotificationSettings
import dev.techm1nd.hydromate.domain.entities.UserSettings
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "NotifScheduler"
        private const val PREFS_NAME = "notification_scheduler"
        private const val KEY_LAST_SCHEDULE_TIMESTAMP = "last_schedule_timestamp"
        private const val KEY_SCHEDULED_SETTINGS_HASH = "scheduled_settings_hash"
        private const val KEY_LAST_SMART_REMINDER_TIME = "last_smart_reminder_time"

        // Request codes для разных типов напоминаний
        private const val SMART_REMINDER_BASE = 10000
        private const val CUSTOM_REMINDER_BASE = 20000
        private const val SNOOZE_REMINDER_CODE = 30000

        const val ACTION_SMART_REMINDER = "dev.techm1nd.hydromate.ACTION_SMART_REMINDER"
        const val ACTION_CUSTOM_REMINDER = "dev.techm1nd.hydromate.ACTION_CUSTOM_REMINDER"
        const val ACTION_SNOOZE_REMINDER = "dev.techm1nd.hydromate.ACTION_SNOOZE_REMINDER"

        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_LABEL = "reminder_label"
        const val EXTRA_REMINDER_INDEX = "reminder_index"
    }

    /**
     * Планирует все напоминания на основе настроек
     * Использует умную проверку для избежания лишних перезаписей
     */
    fun scheduleNotifications(settings: UserSettings) {
        if (!settings.notificationsEnabled) {
            Log.d(TAG, "Notifications disabled, canceling all")
            cancelAllNotifications()
            clearScheduleCache()
            return
        }

        // Проверяем, нужно ли перепланировать уведомления
        if (!shouldReschedule(settings)) {
            Log.d(TAG, "Schedule is up to date, skipping")
            return
        }

        Log.d(TAG, "Scheduling notifications...")

        val notifSettings = settings.toNotificationSettings()

        // Отменяем предыдущие
        cancelAllNotifications()

        val today = LocalDate.now()
        val currentTime = LocalTime.now()

        // Планируем умные напоминания
        if (notifSettings.smartRemindersEnabled) {
            scheduleSmartReminders(notifSettings, today, currentTime)
        }

        // Планируем персональные напоминания
        if (notifSettings.customRemindersEnabled) {
            scheduleCustomReminders(notifSettings, today, currentTime)
        }

        // Сохраняем информацию о планировании
        saveScheduleCache(settings)

        Log.d(TAG, "Notifications scheduled successfully")
    }

    /**
     * Проверяет, нужно ли перепланировать уведомления
     */
    private fun shouldReschedule(settings: UserSettings): Boolean {
        val lastScheduleTimestamp = prefs.getLong(KEY_LAST_SCHEDULE_TIMESTAMP, 0)
        val lastSettingsHash = prefs.getInt(KEY_SCHEDULED_SETTINGS_HASH, 0)
        val currentSettingsHash = settings.getNotificationSettingsHash()

        // Проверяем, изменились ли настройки
        if (lastSettingsHash != currentSettingsHash) {
            Log.d(TAG, "Settings changed, rescheduling needed")
            return true
        }

        // Проверяем, планировались ли уведомления сегодня
        val lastScheduleDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastScheduleTimestamp),
            ZoneId.systemDefault()
        ).toLocalDate()

        if (lastScheduleDate != LocalDate.now()) {
            Log.d(TAG, "Last schedule was on different day, rescheduling needed")
            return true
        }

        return false
    }

    /**
     * Планирует умные напоминания (через интервал)
     * FIXED: Теперь привязано к wake up time, а не текущему времени
     */
    private fun scheduleSmartReminders(
        settings: NotificationSettings,
        date: LocalDate,
        currentTime: LocalTime
    ) {
        val intervalMinutes = settings.reminderInterval.minutes

        val wakeUpTime = settings.wakeUpTime
        val bedTime = settings.bedTime

        // Находим все времена напоминаний на сегодня, начиная с wake up time
        val reminderTimes = generateReminderTimes(
            wakeUpTime = wakeUpTime,
            bedTime = bedTime,
            intervalMinutes = intervalMinutes
        )

        Log.d(TAG, "Generated ${reminderTimes.size} reminder times for today")

        // Планируем только будущие напоминания
        reminderTimes.forEachIndexed { index, reminderTime ->
            if (reminderTime.isAfter(currentTime)) {
                val dateTime = date.atTime(reminderTime)
                scheduleSmartReminder(dateTime, index)
            }
        }

        // Если все напоминания на сегодня прошли, планируем первое на завтра
        if (reminderTimes.all { it.isBefore(currentTime) }) {
            val nextActiveDay = findNextActiveDay(
                startDate = date.plusDays(1),
                enabledDays = settings.smartReminderDays
            )

            nextActiveDay?.let {
                val firstReminderTime = reminderTimes.firstOrNull() ?: wakeUpTime
                val nextDateTime = it.atTime(firstReminderTime)
                scheduleSmartReminder(nextDateTime, 0)
                Log.d(TAG, "Scheduled first reminder for next active day: $nextDateTime")
            }
        }
    }

    /**
     * Генерирует все времена напоминаний для дня
     * Строго привязано к wake up time
     */
    private fun generateReminderTimes(
        wakeUpTime: LocalTime,
        bedTime: LocalTime,
        intervalMinutes: Int
    ): List<LocalTime> {
        val times = mutableListOf<LocalTime>()
        var currentTime = wakeUpTime

        while (currentTime.isBefore(bedTime)) {
            times.add(currentTime)
            currentTime = currentTime.plusMinutes(intervalMinutes.toLong())
        }

        return times
    }

    /**
     * Планирует персональные напоминания
     */
    private fun scheduleCustomReminders(
        settings: NotificationSettings,
        date: LocalDate,
        currentTime: LocalTime
    ) {
        val activeReminders = settings.getActiveCustomRemindersForDay(date.dayOfWeek)

        activeReminders.forEachIndexed { index, reminder ->
            val reminderTime = reminder.getTimeAsLocalTime()

            val reminderDateTime = if (reminderTime.isAfter(currentTime)) {
                // Сегодня
                date.atTime(reminderTime)
            } else {
                // Следующий активный день для этого напоминания
                val nextActiveDay = findNextActiveDayForReminder(
                    startDate = date.plusDays(1),
                    reminder = reminder
                )
                nextActiveDay?.atTime(reminderTime)
            }

            reminderDateTime?.let {
                scheduleCustomReminder(it, reminder.id, reminder.label, index)
            }
        }
    }

    /**
     * Планирует конкретное умное напоминание
     */
    private fun scheduleSmartReminder(dateTime: LocalDateTime, index: Int) {
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = ACTION_SMART_REMINDER
            putExtra(EXTRA_REMINDER_INDEX, index)
        }

        val requestCode = SMART_REMINDER_BASE + index
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleExactAlarm(dateTime, pendingIntent)
        Log.d(TAG, "Smart reminder #$index scheduled for: $dateTime")
    }

    /**
     * Планирует конкретное персональное напоминание
     */
    private fun scheduleCustomReminder(
        dateTime: LocalDateTime,
        reminderId: String,
        label: String,
        index: Int
    ) {
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = ACTION_CUSTOM_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_LABEL, label)
        }

        val requestCode = CUSTOM_REMINDER_BASE + index
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleExactAlarm(dateTime, pendingIntent)
        Log.d(TAG, "Custom reminder '$label' scheduled for: $dateTime")
    }

    /**
     * Планирует отложенное напоминание
     * FIXED: Теперь сохраняет информацию для восстановления после перезагрузки
     */
    fun scheduleSnoozeReminder(settings: UserSettings, snoozeMinutes: Int) {
        if (!settings.snoozeEnabled || snoozeMinutes == 0) return

        val snoozeTime = LocalDateTime.now().plusMinutes(snoozeMinutes.toLong())

        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = ACTION_SNOOZE_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SNOOZE_REMINDER_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleExactAlarm(snoozeTime, pendingIntent)

        // Сохраняем время снуза для восстановления после перезагрузки
        prefs.edit {
            putLong("snooze_scheduled_time", snoozeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }

        Log.d(TAG, "Snooze reminder scheduled for: $snoozeTime")
    }

    /**
     * Планирует напоминания на следующий день после достижения цели
     */
    fun scheduleNextDayReminder(settings: UserSettings) {
        if (!settings.notificationsEnabled) return

        val tomorrow = LocalDate.now().plusDays(1)
        val notifSettings = settings.toNotificationSettings()

        // Проверяем, есть ли активные напоминания на завтра
        if (!notifSettings.hasActiveRemindersForDay(tomorrow.dayOfWeek)) {
            // Ищем следующий активный день
            val nextActiveDay = findNextActiveDayForSettings(tomorrow, notifSettings)
            nextActiveDay?.let {
                val firstReminderTime = notifSettings.wakeUpTime
                val nextDateTime = it.atTime(firstReminderTime)
                scheduleSmartReminder(nextDateTime, 0)
                Log.d(TAG, "Scheduled reminder for next active day after goal reached: $nextDateTime")
            }
        }
    }

    /**
     * Отменяет все напоминания
     */
    fun cancelAllNotifications() {
        // Отменяем умные напоминания (максимум 20 за день)
        for (i in 0..20) {
            cancelPendingIntent(SMART_REMINDER_BASE + i, ACTION_SMART_REMINDER)
        }

        // Отменяем персональные напоминания (максимум 50)
        for (i in 0..50) {
            cancelPendingIntent(CUSTOM_REMINDER_BASE + i, ACTION_CUSTOM_REMINDER)
        }

        // Отменяем отложенные
        cancelPendingIntent(SNOOZE_REMINDER_CODE, ACTION_SNOOZE_REMINDER)

        Log.d(TAG, "All notifications cancelled")
    }

    /**
     * Находит следующий активный день для умных напоминаний
     */
    private fun findNextActiveDay(startDate: LocalDate, enabledDays: Set<DayOfWeek>): LocalDate? {
        var date = startDate
        repeat(7) {
            if (enabledDays.contains(date.dayOfWeek)) {
                return date
            }
            date = date.plusDays(1)
        }
        return null
    }

    /**
     * Находит следующий активный день для персонального напоминания
     */
    private fun findNextActiveDayForReminder(
        startDate: LocalDate,
        reminder: dev.techm1nd.hydromate.domain.entities.CustomReminder
    ): LocalDate? {
        var date = startDate
        repeat(7) {
            if (reminder.isEnabledForDay(date.dayOfWeek)) {
                return date
            }
            date = date.plusDays(1)
        }
        return null
    }

    /**
     * Находит следующий активный день для любых напоминаний
     */
    private fun findNextActiveDayForSettings(
        startDate: LocalDate,
        settings: NotificationSettings
    ): LocalDate? {
        var date = startDate
        repeat(7) {
            if (settings.hasActiveRemindersForDay(date.dayOfWeek)) {
                return date
            }
            date = date.plusDays(1)
        }
        return null
    }

    /**
     * Планирует точный будильник
     */
    private fun scheduleExactAlarm(dateTime: LocalDateTime, pendingIntent: PendingIntent) {
        val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        millis,
                        pendingIntent
                    )
                } else {
                    // Fallback на неточный будильник если нет разрешения
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        millis,
                        pendingIntent
                    )
                    Log.w(TAG, "Exact alarm permission not granted, using inexact alarm")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    millis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    /**
     * Отменяет конкретный PendingIntent
     */
    private fun cancelPendingIntent(requestCode: Int, action: String) {
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            this.action = action
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    /**
     * Сохраняет информацию о планировании в кэш
     */
    private fun saveScheduleCache(settings: UserSettings) {
        prefs.edit {
            putLong(KEY_LAST_SCHEDULE_TIMESTAMP, System.currentTimeMillis())
            putInt(KEY_SCHEDULED_SETTINGS_HASH, settings.getNotificationSettingsHash())
        }
    }

    /**
     * Очищает кэш планирования
     */
    private fun clearScheduleCache() {
        prefs.edit {
            remove(KEY_LAST_SCHEDULE_TIMESTAMP)
            remove(KEY_SCHEDULED_SETTINGS_HASH)
        }
    }

    /**
     * Восстанавливает отложенное напоминание после перезагрузки
     */
    fun restoreSnoozeReminderIfNeeded(settings: UserSettings) {
        val snoozeScheduledTime = prefs.getLong("snooze_scheduled_time", 0)
        if (snoozeScheduledTime > 0) {
            val snoozeDateTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(snoozeScheduledTime),
                ZoneId.systemDefault()
            )

            // Проверяем, не прошло ли время
            if (snoozeDateTime.isAfter(LocalDateTime.now())) {
                val intent = Intent(context, WaterReminderReceiver::class.java).apply {
                    action = ACTION_SNOOZE_REMINDER
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    SNOOZE_REMINDER_CODE,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                scheduleExactAlarm(snoozeDateTime, pendingIntent)
                Log.d(TAG, "Restored snooze reminder for: $snoozeDateTime")
            } else {
                // Время прошло, очищаем
                prefs.edit { remove("snooze_scheduled_time") }
            }
        }
    }
}

/**
 * Расширение для получения хэша настроек уведомлений
 * Используется для определения изменений настроек
 */
private fun UserSettings.getNotificationSettingsHash(): Int {
    var result = notificationsEnabled.hashCode()
    result = 31 * result + wakeUpTime.hashCode()
    result = 31 * result + bedTime.hashCode()
    result = 31 * result + smartRemindersEnabled.hashCode()
    result = 31 * result + reminderInterval.hashCode()
    result = 31 * result + smartReminderDays.hashCode()
    result = 31 * result + customRemindersEnabled.hashCode()
    result = 31 * result + customReminders.hashCode()
    result = 31 * result + snoozeEnabled.hashCode()
    result = 31 * result + snoozeDelay.hashCode()
    return result
}

/**
 * Расширение для доступа к Context из NotificationScheduler
 * Используется в WaterReminderReceiver для планирования следующих напоминаний
 */
val NotificationScheduler.context: Context
    get() = this.javaClass.getDeclaredField("context").let { field ->
        field.isAccessible = true
        field.get(this) as Context
    }