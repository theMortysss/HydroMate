package dev.techm1nd.hydromate.domain.entities

import java.time.LocalDateTime

data class WaterEntry(
    val id: Long = 0,
    val amount: Int, // в мл
    val timestamp: LocalDateTime,
    val drinkId: Long = 1, // ID напитка (по умолчанию вода)
    val type: DrinkType = DrinkType.WATER
)
