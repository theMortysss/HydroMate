package sdf.bitt.hydromate.ui.screens.statistics

import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.WeeklyStatistics
import sdf.bitt.hydromate.domain.usecases.TotalHydration
import java.time.LocalDate

data class StatisticsUiState(
    val weeklyStats: WeeklyStatistics? = null,
    val selectedWeekStart: LocalDate = getCurrentWeekStart(),
    val isLoading: Boolean = false,
    val error: String? = null,

    // NEW: Данные гидратации
    val showNetHydration: Boolean = true,
    val hydrationData: TotalHydration? = null
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