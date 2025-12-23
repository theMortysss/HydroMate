package dev.techm1nd.hydromate.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,

    // Прогрессия
    @ColumnInfo(name = "level")
    val level: Int = 1,

    @ColumnInfo(name = "current_xp")
    val currentXP: Int = 0,

    @ColumnInfo(name = "total_xp")
    val totalXP: Int = 0,

    @ColumnInfo(name = "selected_character")
    val selectedCharacter: String = "PENGUIN",

    @ColumnInfo(name = "unlocked_characters")
    val unlockedCharacters: String = "[]", // JSON array

    // Статистика
    @ColumnInfo(name = "total_drinks_drank")
    val totalDrinksDrank: Int = 0,

    @ColumnInfo(name = "unique_drinks_tried")
    val uniqueDrinksTried: String = "[]", // JSON array

    @ColumnInfo(name = "challenges_completed")
    val challengesCompleted: Int = 0,

    @ColumnInfo(name = "achievements_unlocked")
    val achievementsUnlocked: Int = 0
)
