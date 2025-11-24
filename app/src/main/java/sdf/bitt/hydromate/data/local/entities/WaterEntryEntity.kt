package sdf.bitt.hydromate.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long, // Unix timestamp в секундах

    @ColumnInfo(name = "drink_type")
    val drinkType: String, // Deprecated: оставлен для обратной совместимости

    @ColumnInfo(name = "drink_id", defaultValue = "1")
    val drinkId: Long = 1 // ID напитка из таблицы drinks
)