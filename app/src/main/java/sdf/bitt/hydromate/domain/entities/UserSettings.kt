package sdf.bitt.hydromate.domain.entities

import java.time.DayOfWeek
import java.time.LocalTime

data class UserSettings(
    val dailyGoal: Int = 2000,
    val selectedCharacter: CharacterType = CharacterType.PENGUIN,

    // Базовые настройки уведомлений
    val notificationsEnabled: Boolean = true,
    val wakeUpTime: LocalTime = LocalTime.of(8, 0),
    val bedTime: LocalTime = LocalTime.of(22, 0),

    // Умные напоминания (через интервал)
    val smartRemindersEnabled: Boolean = true,
    val notificationInterval: Int = 60, // минут (для обратной совместимости)
    val reminderInterval: ReminderInterval = ReminderInterval.HOUR_1,
    val smartReminderDays: Set<DayOfWeek> = DayOfWeek.values().toSet(),

    // Персональные напоминания (в конкретное время)
    val customRemindersEnabled: Boolean = false,
    val customReminders: List<CustomReminder> = emptyList(),

    // Отсрочка уведомлений
    val snoozeEnabled: Boolean = true,
    val snoozeDelay: SnoozeDelay = SnoozeDelay.MIN_10,

    // Прогресс в уведомлениях
    val showProgressInNotification: Boolean = true,

    // Остальные настройки
    val quickAddPresets: List<QuickAddPreset> = QuickAddPreset.getDefaults(),
    val showNetHydration: Boolean = true,
    val profile: UserProfile = UserProfile()
) {
    @Deprecated("Use quickAddPresets instead")
    val quickAmounts: List<Int>
        get() = quickAddPresets.map { it.amount }

    fun getEffectiveGoal(): Int {
        return if (profile.isManualGoal) {
            profile.manualGoal
        } else {
            dailyGoal
        }
    }

    /**
     * Конвертация в NotificationSettings
     */
    fun toNotificationSettings(): NotificationSettings {
        return NotificationSettings(
            notificationsEnabled = notificationsEnabled,
            wakeUpTime = wakeUpTime,
            bedTime = bedTime,
            smartRemindersEnabled = smartRemindersEnabled,
            reminderInterval = reminderInterval,
            smartReminderDays = smartReminderDays,
            customRemindersEnabled = customRemindersEnabled,
            customReminders = customReminders,
            snoozeEnabled = snoozeEnabled,
            snoozeDelay = snoozeDelay,
            showProgressInNotification = showProgressInNotification
        )
    }

    /**
     * Проверяет, активны ли умные напоминания для текущего дня
     */
    fun isSmartReminderEnabledToday(): Boolean {
        val today = java.time.LocalDate.now().dayOfWeek
        return notificationsEnabled && smartRemindersEnabled && smartReminderDays.contains(today)
    }

    /**
     * Получает активные персональные напоминания для текущего дня
     */
    fun getActiveCustomRemindersToday(): List<CustomReminder> {
        if (!notificationsEnabled || !customRemindersEnabled) return emptyList()
        val today = java.time.LocalDate.now().dayOfWeek
        return customReminders.filter { it.isEnabledForDay(today) }
    }
}

enum class CharacterType(val displayName: String, val emoji: String) {
    PENGUIN("Penguin", "🐧"),
    CAT("Cat", "🐱"),
    FROG("Frog", "🐸"),
    DUCK("Duck", "🦆"),
    FISH("Fish", "🐠")
}