package dev.techm1nd.hydromate.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "challenges",
    indices = [Index(value = ["is_active"]), Index(value = ["type"])]
)
data class ChallengeEntity(
    @PrimaryKey
    val id: String = "",

    @ColumnInfo(name = "type")
    val type: String = "",

    @ColumnInfo(name = "duration_days")
    val durationDays: Int = 0,

    @ColumnInfo(name = "start_date")
    val startDate: Long = 0L,

    @ColumnInfo(name = "end_date")
    val endDate: Long = 0L,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,

    @ColumnInfo(name = "violations")
    val violations: String = "[]",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
