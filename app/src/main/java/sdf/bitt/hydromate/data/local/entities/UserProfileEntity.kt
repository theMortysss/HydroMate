package sdf.bitt.hydromate.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,

    // Прогрессия
    @ColumnInfo(name = "level")
    val level: Int,

    @ColumnInfo(name = "current_xp")
    val currentXP: Int,

    @ColumnInfo(name = "total_xp")
    val totalXP: Int,

    @ColumnInfo(name = "selected_character")
    val selectedCharacter: String,

    @ColumnInfo(name = "unlocked_characters")
    val unlockedCharacters: String, // JSON array

    // Статистика
    @ColumnInfo(name = "total_drinks_drank")
    val totalDrinksDrank: Int,

    @ColumnInfo(name = "unique_drinks_tried")
    val uniqueDrinksTried: String, // JSON array

    @ColumnInfo(name = "challenges_completed")
    val challengesCompleted: Int,

    @ColumnInfo(name = "achievements_unlocked")
    val achievementsUnlocked: Int
)
