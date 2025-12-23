package dev.techm1nd.hydromate.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "achievements",
    indices = [Index(value = ["is_unlocked"]), Index(value = ["type"])]
)
data class AchievementEntity(
    @PrimaryKey
    val id: String = "",

    @ColumnInfo(name = "type")
    val type: String = "",

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "icon")
    val icon: String = "",

    @ColumnInfo(name = "xp_reward")
    val xpReward: Int = 0,

    @ColumnInfo(name = "is_unlocked")
    val isUnlocked: Boolean = false,

    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long? = null,

    @ColumnInfo(name = "progress")
    val progress: Int = 0,

    @ColumnInfo(name = "progress_max")
    val progressMax: Int = 0,

    @ColumnInfo(name = "unlockable_character")
    val unlockableCharacter: String? = null
)
