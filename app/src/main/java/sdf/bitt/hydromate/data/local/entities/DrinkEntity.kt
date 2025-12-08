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
    val category: String, // храним как String (название enum)

    @ColumnInfo(name = "caffeine_content")
    val caffeineContent: Int = 0, // мг на 250 мл

    @ColumnInfo(name = "alcohol_percentage")
    val alcoholPercentage: Float = 0f, // %

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,

    @ColumnInfo(name = "color")
    val color: String = "#2196F3",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)