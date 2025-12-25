package dev.techm1nd.hydromate.domain.entities

import java.time.LocalDate

data class WeeklyStatistics(
    val weekStart: LocalDate = LocalDate.now().minusWeeks(1),
    val weekEnd: LocalDate = LocalDate.now(),
    val dailyProgress: List<DailyProgress> = emptyList(),
    val totalAmount: Int = 0,
    val averageDaily: Int = 0,
    val daysGoalReached: Int = 0,
    val currentStreak: Int = 0
) {
    val weeklyGoal: Int
        get() = dailyProgress.firstOrNull()?.goalAmount?.times(7) ?: 14000

    val weekProgressPercentage: Float
        get() = (totalAmount.toFloat() / weeklyGoal).coerceAtMost(1f)
}