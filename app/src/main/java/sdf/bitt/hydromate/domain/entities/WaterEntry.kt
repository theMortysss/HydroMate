package sdf.bitt.hydromate.domain.entities

import java.time.LocalDateTime

data class WaterEntry(
    val id: Long = 0,
    val amount: Int, // в мл
    val timestamp: LocalDateTime,
    val type: DrinkType = DrinkType.WATER
)

enum class DrinkType(val displayName: String, val icon: String) {
    WATER("Water", "💧"),
    TEA("Tea", "🍵"),
    COFFEE("Coffee", "☕"),
    JUICE("Juice", "🧃"),
    OTHER("Other", "🥤")
}