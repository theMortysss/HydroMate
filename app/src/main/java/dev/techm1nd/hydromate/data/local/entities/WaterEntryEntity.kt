package dev.techm1nd.hydromate.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "water_entries",
    indices = [Index(value = ["timestamp"])]
)
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Int = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = 0L, // Unix timestamp в секундах

    @ColumnInfo(name = "drink_type")
    val drinkType: String = "", // Deprecated: оставлен для обратной совместимости

    @ColumnInfo(name = "drink_id", defaultValue = "1")
    val drinkId: Long = 1 // ID напитка из таблицы drinks
)