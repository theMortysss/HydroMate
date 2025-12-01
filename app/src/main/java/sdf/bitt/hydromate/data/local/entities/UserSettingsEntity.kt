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

    // UPDATED: Храним JSON с полными пресетами
    @ColumnInfo(name = "quick_add_presets", defaultValue = "[]")
    val quickAddPresets: String, // JSON string: List<QuickAddPreset>

    // Старое поле для обратной совместимости
    @ColumnInfo(name = "quick_amounts")
    val quickAmounts: String = "[]", // Deprecated

    @ColumnInfo(name = "hydration_threshold", defaultValue = "1.0")
    val hydrationThreshold: Float = 1.0f,

    @ColumnInfo(name = "show_net_hydration", defaultValue = "1")
    val showNetHydration: Boolean = true
)