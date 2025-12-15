package sdf.bitt.hydromate.data.local.entities

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
    val id: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "duration_days")
    val durationDays: Int,

    @ColumnInfo(name = "start_date")
    val startDate: Long, // epoch day

    @ColumnInfo(name = "end_date")
    val endDate: Long, // epoch day

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    @ColumnInfo(name = "current_streak")
    val currentStreak: Int,

    @ColumnInfo(name = "violations")
    val violations: String, // JSON

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
