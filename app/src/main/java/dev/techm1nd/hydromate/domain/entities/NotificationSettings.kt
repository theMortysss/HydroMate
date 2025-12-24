package dev.techm1nd.hydromate.domain.entities

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Тип напоминания
 */
enum class ReminderType {
    SMART,      // Умные (через интервал)
    CUSTOM      // Персональные (в конкретное время)
}

/**
 * Интервал для умных напоминаний
 */
enum class ReminderInterval(val displayName: String, val minutes: Int) {
    MIN_30("Every 30 minutes", 30),
    HOUR_1("Every hour", 60),
    HOUR_2("Every 2 hours", 120),
    HOUR_3("Every 3 hours", 180),
    HOUR_4("Every 4 hours", 240);

    companion object {
        fun fromMinutes(minutes: Int): ReminderInterval {
            return entries.find { it.minutes == minutes } ?: HOUR_1
        }
    }
}

/**
 * Время отсрочки уведомления
 */
enum class SnoozeDelay(val displayName: String, val minutes: Int) {
    DISABLED("Disabled", 0),
    MIN_5("5 minutes", 5),
    MIN_10("10 minutes", 10),
    MIN_15("15 minutes", 15),
    MIN_30("30 minutes", 30);

    companion object {
        fun fromMinutes(minutes: Int): SnoozeDelay {
            return values().find { it.minutes == minutes } ?: DISABLED
        }
    }
}

/**
 * Персональное напоминание
 */
@Serializable
data class CustomReminder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val time: String, // "HH:mm" format
    val label: String = "",
    val enabledDays: Set<String> = DayOfWeek.entries.map { it.name }.toSet(),
    val isEnabled: Boolean = true
) {
    fun getTimeAsLocalTime(): LocalTime {
        val parts = time.split(":")
        return LocalTime.of(parts[0].toInt(), parts[1].toInt())
    }

    fun isEnabledForDay(dayOfWeek: DayOfWeek): Boolean {
        return isEnabled && enabledDays.contains(dayOfWeek.name)
    }

    companion object {
        fun getDefaults(): List<CustomReminder> = listOf(
            CustomReminder(
                time = "09:00",
                label = "Morning hydration",
                enabledDays = setOf(
                    DayOfWeek.MONDAY.name,
                    DayOfWeek.TUESDAY.name,
                    DayOfWeek.WEDNESDAY.name,
                    DayOfWeek.THURSDAY.name,
                    DayOfWeek.FRIDAY.name
                )
            ),
            CustomReminder(
                time = "15:00",
                label = "Afternoon hydration"
            )
        )
    }
}

/**
 * Расширенные настройки уведомлений
 */
data class NotificationSettings(
    // Базовые настройки
    val notificationsEnabled: Boolean = true,
    val wakeUpTime: LocalTime = LocalTime.of(8, 0),
    val bedTime: LocalTime = LocalTime.of(22, 0),

    // Умные напоминания
    val smartRemindersEnabled: Boolean = true,
    val reminderInterval: ReminderInterval = ReminderInterval.HOUR_1,
    val smartReminderDays: Set<DayOfWeek> = DayOfWeek.values().toSet(),

    // Персональные напоминания
    val customRemindersEnabled: Boolean = false,
    val customReminders: List<CustomReminder> = emptyList(),

    // Отсрочка
    val snoozeEnabled: Boolean = true,
    val snoozeDelay: SnoozeDelay = SnoozeDelay.MIN_10,

    // Отображение прогресса
    val showProgressInNotification: Boolean = true
) {
    /**
     * Проверяет, активны ли умные напоминания для конкретного дня
     */
    fun isSmartReminderEnabledForDay(dayOfWeek: DayOfWeek): Boolean {
        return notificationsEnabled && smartRemindersEnabled && smartReminderDays.contains(dayOfWeek)
    }

    /**
     * Получает активные персональные напоминания для дня
     */
    fun getActiveCustomRemindersForDay(dayOfWeek: DayOfWeek): List<CustomReminder> {
        if (!notificationsEnabled || !customRemindersEnabled) return emptyList()
        return customReminders.filter { it.isEnabledForDay(dayOfWeek) }
    }

    /**
     * Проверяет, активны ли какие-либо напоминания для дня
     */
    fun hasActiveRemindersForDay(dayOfWeek: DayOfWeek): Boolean {
        return isSmartReminderEnabledForDay(dayOfWeek) ||
                getActiveCustomRemindersForDay(dayOfWeek).isNotEmpty()
    }
}

/**
 * Расширение UserSettings с новыми настройками уведомлений
 */
fun UserSettings.toNotificationSettings(): NotificationSettings {
    // Будет реализовано после обновления UserSettings
    return NotificationSettings(
        notificationsEnabled = this.notificationsEnabled,
        wakeUpTime = this.wakeUpTime,
        bedTime = this.bedTime
    )
}