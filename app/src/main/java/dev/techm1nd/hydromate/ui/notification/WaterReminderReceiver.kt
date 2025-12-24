package dev.techm1nd.hydromate.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.repositories.DrinkRepository
import dev.techm1nd.hydromate.domain.usecases.hydration.CalculateHydrationUseCase
import dev.techm1nd.hydromate.domain.usecases.stat.GetTodayProgressUseCase
import dev.techm1nd.hydromate.domain.usecases.setting.GetUserSettingsUseCase
import javax.inject.Inject

@AndroidEntryPoint
class WaterReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: HydroMateNotificationManager

    @Inject
    lateinit var getTodayProgressUseCase: GetTodayProgressUseCase

    @Inject
    lateinit var getUserSettingsUseCase: GetUserSettingsUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var drinkRepository: DrinkRepository

    @Inject
    lateinit var calculateHydrationUseCase: CalculateHydrationUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        scope.launch {
            try {
                val settings = getUserSettingsUseCase().first()

                if (!settings.notificationsEnabled) {
                    Log.d(TAG, "Notifications disabled, skipping")
                    return@launch
                }

                when (action) {
                    NotificationScheduler.ACTION_SMART_REMINDER -> {
                        val index = intent.getIntExtra(NotificationScheduler.EXTRA_REMINDER_INDEX, -1)
                        handleSmartReminder(context, settings, index)
                    }
                    NotificationScheduler.ACTION_CUSTOM_REMINDER -> {
                        val reminderId = intent.getStringExtra(NotificationScheduler.EXTRA_REMINDER_ID)
                        val label = intent.getStringExtra(NotificationScheduler.EXTRA_REMINDER_LABEL)
                        handleCustomReminder(context, settings, reminderId, label)
                    }
                    NotificationScheduler.ACTION_SNOOZE_REMINDER -> {
                        handleSnoozeReminder(context, settings)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing reminder", e)
            }
        }
    }

    private suspend fun handleSmartReminder(
        context: Context,
        settings: dev.techm1nd.hydromate.domain.entities.UserSettings,
        reminderIndex: Int
    ) {
        val progress = getTodayProgressUseCase().first()
        val drinks = drinkRepository.getAllActiveDrinks().first()
        val drinksMap = drinks.associateBy { it.id }

        val hydration = if (progress.entries.isNotEmpty()) {
            calculateHydrationUseCase.calculateTotal(progress.entries, drinksMap)
        } else {
            null
        }

        val currentAmount = hydration?.netHydration ?: 0

        val adjustedGoal = progress.goalAmount
        val isGoalReached = currentAmount >= adjustedGoal

        if (isGoalReached) {
            Log.d(TAG, "Goal reached, skipping smart reminder #$reminderIndex")

            // Показываем поздравление только один раз в день
            if (!hasShownCongratulationsToday(context)) {
                notificationManager.showGoalAchievedNotification(
                    currentAmount = currentAmount,
                    goalAmount = adjustedGoal,
                    showProgress = settings.showProgressInNotification
                )
                markCongratulationsShown(context)
            }

            // Планируем напоминания на следующий день
            notificationScheduler.scheduleNextDayReminder(settings)
            return
        }

        // Показываем напоминание
        notificationManager.showHydrationReminder(
            currentAmount = currentAmount,
            goalAmount = adjustedGoal,
            showProgress = settings.showProgressInNotification,
            canSnooze = settings.snoozeEnabled,
            snoozeMinutes = settings.snoozeDelay.minutes
        )

        // Планируем следующее напоминание в цепочке
        scheduleNextSmartReminder(settings, reminderIndex)
    }

    /**
     * Планирует следующее напоминание в цепочке
     * FIXED: Теперь правильно работает с цепочкой от wake up time
     */
    private fun scheduleNextSmartReminder(
        settings: dev.techm1nd.hydromate.domain.entities.UserSettings,
        currentIndex: Int
    ) {
        val notifSettings = settings.toNotificationSettings()
        val intervalMinutes = notifSettings.reminderInterval.minutes
        val wakeUpTime = notifSettings.wakeUpTime
        val bedTime = notifSettings.bedTime

        // Генерируем все времена напоминаний для дня
        val reminderTimes = generateReminderTimes(wakeUpTime, bedTime, intervalMinutes)

        // Берем следующее время в цепочке
        val nextIndex = currentIndex + 1

        if (nextIndex < reminderTimes.size) {
            // Есть еще напоминания на сегодня
            val nextTime = reminderTimes[nextIndex]
            val now = java.time.LocalTime.now()

            if (nextTime.isAfter(now)) {
                val nextDateTime = java.time.LocalDate.now().atTime(nextTime)
                scheduleSmartReminderAtIndex(nextDateTime, nextIndex)
                Log.d(TAG, "Scheduled next smart reminder #$nextIndex for: $nextDateTime")
            }
        } else {
            // Все напоминания на сегодня закончились, планируем на завтра
            val tomorrow = java.time.LocalDate.now().plusDays(1)
            val nextActiveDay = findNextActiveDay(tomorrow, notifSettings.smartReminderDays)

            nextActiveDay?.let {
                val firstReminderTime = reminderTimes.firstOrNull() ?: wakeUpTime
                val nextDateTime = it.atTime(firstReminderTime)
                scheduleSmartReminderAtIndex(nextDateTime, 0)
                Log.d(TAG, "Scheduled first reminder for next day: $nextDateTime")
            }
        }
    }

    private fun generateReminderTimes(
        wakeUpTime: java.time.LocalTime,
        bedTime: java.time.LocalTime,
        intervalMinutes: Int
    ): List<java.time.LocalTime> {
        val times = mutableListOf<java.time.LocalTime>()
        var currentTime = wakeUpTime

        while (currentTime.isBefore(bedTime)) {
            times.add(currentTime)
            currentTime = currentTime.plusMinutes(intervalMinutes.toLong())
        }

        return times
    }

    private fun scheduleSmartReminderAtIndex(
        dateTime: java.time.LocalDateTime,
        index: Int
    ) {
        val intent = Intent(notificationScheduler.context, WaterReminderReceiver::class.java).apply {
            action = NotificationScheduler.ACTION_SMART_REMINDER
            putExtra(NotificationScheduler.EXTRA_REMINDER_INDEX, index)
        }

        val requestCode = 10000 + index
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            notificationScheduler.context,
            requestCode,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val millis = dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val alarmManager = notificationScheduler.context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        millis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        millis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    millis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule next reminder", e)
        }
    }

    private fun findNextActiveDay(
        startDate: java.time.LocalDate,
        enabledDays: Set<java.time.DayOfWeek>
    ): java.time.LocalDate? {
        var date = startDate
        repeat(7) {
            if (enabledDays.contains(date.dayOfWeek)) {
                return date
            }
            date = date.plusDays(1)
        }
        return null
    }

    private suspend fun handleCustomReminder(
        context: Context,
        settings: dev.techm1nd.hydromate.domain.entities.UserSettings,
        reminderId: String?,
        label: String?
    ) {
        val progress = getTodayProgressUseCase().first()
        val drinks = drinkRepository.getAllActiveDrinks().first()
        val drinksMap = drinks.associateBy { it.id }

        val hydration = if (progress.entries.isNotEmpty()) {
            calculateHydrationUseCase.calculateTotal(progress.entries, drinksMap)
        } else {
            null
        }

        val currentAmount = hydration?.netHydration ?: 0

        val adjustedGoal = progress.goalAmount
        val isGoalReached = currentAmount >= adjustedGoal

        // Показываем персональное напоминание даже если цель достигнута
        notificationManager.showCustomHydrationReminder(
            currentAmount = currentAmount,
            goalAmount = adjustedGoal,
            reminderLabel = label ?: "Time to hydrate",
            showProgress = settings.showProgressInNotification,
            canSnooze = settings.snoozeEnabled,
            snoozeMinutes = settings.snoozeDelay.minutes,
            isGoalReached = isGoalReached
        )

        // Планируем это напоминание на следующий активный день
        reminderId?.let { id ->
            val reminder = settings.customReminders.find { it.id == id }
            reminder?.let {
                val tomorrow = java.time.LocalDate.now().plusDays(1)
                val nextActiveDay = findNextActiveDayForReminder(tomorrow, it)

                nextActiveDay?.let { date ->
                    val reminderTime = it.getTimeAsLocalTime()
                    val nextDateTime = date.atTime(reminderTime)
                    // Будет запланировано через scheduleNotifications при следующем вызове
                    Log.d(TAG, "Custom reminder will be rescheduled for: $nextDateTime")
                }
            }
        }
    }

    private fun findNextActiveDayForReminder(
        startDate: java.time.LocalDate,
        reminder: dev.techm1nd.hydromate.domain.entities.CustomReminder
    ): java.time.LocalDate? {
        var date = startDate
        repeat(7) {
            if (reminder.isEnabledForDay(date.dayOfWeek)) {
                return date
            }
            date = date.plusDays(1)
        }
        return null
    }

    private suspend fun handleSnoozeReminder(
        context: Context,
        settings: dev.techm1nd.hydromate.domain.entities.UserSettings
    ) {
        // Обрабатываем как обычное умное напоминание
        handleSmartReminder(context, settings, -1) // index -1 для snooze

        // Очищаем сохраненное время снуза
        context.getSharedPreferences("notification_scheduler", Context.MODE_PRIVATE).edit {
            remove("snooze_scheduled_time")
        }
    }

    private fun hasShownCongratulationsToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCongratulation = prefs.getLong(KEY_LAST_CONGRATULATION, 0)
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastDay = lastCongratulation / (24 * 60 * 60 * 1000)
        return today == lastDay
    }

    private fun markCongratulationsShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_LAST_CONGRATULATION, System.currentTimeMillis()) }
    }

    companion object {
        private const val TAG = "WaterReminderReceiver"
        private const val PREFS_NAME = "hydro_mate_notifications"
        private const val KEY_LAST_CONGRATULATION = "last_congratulation_time"
    }
}