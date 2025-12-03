package sdf.bitt.hydromate.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import sdf.bitt.hydromate.domain.entities.WeeklyStatistics
import sdf.bitt.hydromate.domain.repositories.WaterRepository
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklyStatisticsUseCase @Inject constructor(
    private val repository: WaterRepository
) {
    operator fun invoke(startDate: LocalDate = getWeekStart()): Flow<WeeklyStatistics> {
        val endDate = startDate.plusDays(6)

        // Создаем список Flow для каждого дня недели
        val dailyFlows = (0..6).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            repository.getProgressForDate(date)
        }

        // Комбинируем все дни в одну статистику
        return combine(dailyFlows) { dailyProgressArray ->
            val dailyProgressList = dailyProgressArray.toList()

            val totalAmount = dailyProgressList.sumOf { it.totalAmount }
            val averageDaily = if (dailyProgressList.isNotEmpty()) totalAmount / dailyProgressList.size else 0
            val daysGoalReached = dailyProgressList.count { it.isGoalReached }
            val currentStreak = calculateStreak(dailyProgressList)

            WeeklyStatistics(
                weekStart = startDate,
                weekEnd = endDate,
                dailyProgress = dailyProgressList,
                totalAmount = totalAmount,
                averageDaily = averageDaily,
                daysGoalReached = daysGoalReached,
                currentStreak = currentStreak
            )
        }
    }

    private fun calculateStreak(dailyProgress: List<sdf.bitt.hydromate.domain.entities.DailyProgress>): Int {
        var streak = 0
        for (progress in dailyProgress.reversed()) {
            if (progress.isGoalReached) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun getWeekStart(): LocalDate {
        val now = LocalDate.now()
        val dayOfWeek = now.dayOfWeek.value
        return now.minusDays((dayOfWeek - 1).toLong())
    }
}
