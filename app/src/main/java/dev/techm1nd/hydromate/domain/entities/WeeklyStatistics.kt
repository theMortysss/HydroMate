package dev.techm1nd.hydromate.domain.entities

import java.time.LocalDate

data class WeeklyStatistics(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val dailyProgress: List<DailyProgress>,
    val totalAmount: Int,
    val averageDaily: Int,
    val daysGoalReached: Int,
    val currentStreak: Int
) {
    val weeklyGoal: Int
        get() = dailyProgress.firstOrNull()?.goalAmount?.times(7) ?: 14000

    val weekProgressPercentage: Float
        get() = (totalAmount.toFloat() / weeklyGoal).coerceAtMost(1f)
}