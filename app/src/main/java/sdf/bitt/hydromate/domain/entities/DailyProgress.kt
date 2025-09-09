package sdf.bitt.hydromate.domain.entities

import java.time.LocalDate

data class DailyProgress(
    val date: LocalDate,
    val totalAmount: Int,
    val goalAmount: Int,
    val entries: List<WaterEntry>,
    val streak: Int = 0,
    val achievementUnlocked: Achievement? = null
) {
    val progressPercentage: Float
        get() = (totalAmount.toFloat() / goalAmount).coerceAtMost(1f)

    val isGoalReached: Boolean
        get() = totalAmount >= goalAmount

    val remainingAmount: Int
        get() = (goalAmount - totalAmount).coerceAtLeast(0)
}