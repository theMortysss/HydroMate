package dev.techm1nd.hydromate.ui.screens.statistics.model

import dev.techm1nd.hydromate.domain.entities.WeeklyStatistics
import dev.techm1nd.hydromate.domain.usecases.hydration.TotalHydration
import java.time.LocalDate

data class StatisticsState(
    val weeklyStats: WeeklyStatistics = WeeklyStatistics(),
    val selectedWeekStart: LocalDate = getCurrentWeekStart(),
    val isLoading: Boolean = false,
    val error: String? = null,

    val hydrationData: TotalHydration = TotalHydration()
)

sealed class StatisticsIntent {
    object RefreshData : StatisticsIntent()
    data class SelectWeek(val weekStart: LocalDate) : StatisticsIntent()
    object PreviousWeek : StatisticsIntent()
    object NextWeek : StatisticsIntent()
    object ClearError : StatisticsIntent()
}

private fun getCurrentWeekStart(): LocalDate {
    val now = LocalDate.now()
    val dayOfWeek = now.dayOfWeek.value
    return now.minusDays((dayOfWeek - 1).toLong())
}