package dev.techm1nd.hydromate.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "daily_goal")
    val dailyGoal: Int,

//    @ColumnInfo(name = "selected_character")
//    val selectedCharacter: String,

    // Базовые настройки уведомлений
    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean,

    @ColumnInfo(name = "wake_up_time")
    val wakeUpTime: String, // "HH:mm" format

    @ColumnInfo(name = "bed_time")
    val bedTime: String, // "HH:mm" format

    // Умные напоминания
    @ColumnInfo(name = "smart_reminders_enabled", defaultValue = "1")
    val smartRemindersEnabled: Boolean = true,

    @ColumnInfo(name = "notification_interval") // Deprecated, для обратной совместимости
    val notificationInterval: Int = 60,

    @ColumnInfo(name = "reminder_interval_minutes", defaultValue = "60")
    val reminderIntervalMinutes: Int = 60,

    @ColumnInfo(name = "smart_reminder_days", defaultValue = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY")
    val smartReminderDays: String = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY",

    // Персональные напоминания
    @ColumnInfo(name = "custom_reminders_enabled", defaultValue = "0")
    val customRemindersEnabled: Boolean = false,

    @ColumnInfo(name = "custom_reminders", defaultValue = "[]")
    val customReminders: String = "[]", // JSON: List<CustomReminder>

    // Отсрочка
    @ColumnInfo(name = "snooze_enabled", defaultValue = "1")
    val snoozeEnabled: Boolean = true,

    @ColumnInfo(name = "snooze_delay_minutes", defaultValue = "10")
    val snoozeDelayMinutes: Int = 10,

    // Прогресс в уведомлениях
    @ColumnInfo(name = "show_progress_in_notification", defaultValue = "1")
    val showProgressInNotification: Boolean = true,

    // Quick add presets
    @ColumnInfo(name = "quick_add_presets", defaultValue = "[]")
    val quickAddPresets: String,

    @ColumnInfo(name = "quick_amounts")
    val quickAmounts: String = "[]", // Deprecated

    @ColumnInfo(name = "show_net_hydration", defaultValue = "1")
    val showNetHydration: Boolean = true,

    // Hydration profile fields
    @ColumnInfo(name = "profile_gender", defaultValue = "PREFER_NOT_TO_SAY")
    val profileGender: String = "PREFER_NOT_TO_SAY",

    @ColumnInfo(name = "profile_weight_kg", defaultValue = "70")
    val profileWeightKg: Int = 70,

    @ColumnInfo(name = "profile_activity_level", defaultValue = "MODERATE")
    val profileActivityLevel: String = "MODERATE",

    @ColumnInfo(name = "profile_climate", defaultValue = "MODERATE")
    val profileClimate: String = "MODERATE",

    @ColumnInfo(name = "is_manual_goal", defaultValue = "0")
    val manualGoalEnabled: Boolean = false,

    @ColumnInfo(name = "manual_goal", defaultValue = "2000")
    val manualGoalMl: Int = 2000
)