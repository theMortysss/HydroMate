package sdf.bitt.hydromate.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: String,

    @ColumnInfo(name = "hydration_multiplier")
    val hydrationMultiplier: Float,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "contains_caffeine")
    val containsCaffeine: Boolean = false,

    @ColumnInfo(name = "contains_alcohol")
    val containsAlcohol: Boolean = false,

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,

    @ColumnInfo(name = "color")
    val color: String = "#2196F3",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)