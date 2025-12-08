package sdf.bitt.hydromate.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "daily_goal")
    val dailyGoal: Int,

    @ColumnInfo(name = "selected_character")
    val selectedCharacter: String,

    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean,

    @ColumnInfo(name = "notification_interval")
    val notificationInterval: Int,

    @ColumnInfo(name = "wake_up_time")
    val wakeUpTime: String, // "HH:mm" format

    @ColumnInfo(name = "bed_time")
    val bedTime: String, // "HH:mm" format

    @ColumnInfo(name = "quick_add_presets", defaultValue = "[]")
    val quickAddPresets: String, // JSON string: List<QuickAddPreset>

    @ColumnInfo(name = "quick_amounts")
    val quickAmounts: String = "[]", // Deprecated

    @ColumnInfo(name = "show_net_hydration", defaultValue = "1")
    val showNetHydration: Boolean = true,

    // NEW: Hydration profile fields
    @ColumnInfo(name = "profile_gender", defaultValue = "PREFER_NOT_TO_SAY")
    val profileGender: String = "PREFER_NOT_TO_SAY",

    @ColumnInfo(name = "profile_weight_kg", defaultValue = "70")
    val profileWeightKg: Int = 70,

    @ColumnInfo(name = "profile_activity_level", defaultValue = "MODERATE")
    val profileActivityLevel: String = "MODERATE",

    @ColumnInfo(name = "profile_climate", defaultValue = "MODERATE")
    val profileClimate: String = "MODERATE",

    @ColumnInfo(name = "is_manual_goal", defaultValue = "0")
    val isManualGoal: Boolean = false,

    @ColumnInfo(name = "manual_goal", defaultValue = "2000")
    val manualGoal: Int = 2000
)