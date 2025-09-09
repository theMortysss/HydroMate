package sdf.bitt.hydromate.ui.screens.history

import sdf.bitt.hydromate.domain.entities.DailyProgress
import java.time.LocalDate
import java.time.YearMonth

data class HistoryUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthlyProgress: Map<LocalDate, DailyProgress> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val selectedDateProgress: DailyProgress? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class HistoryIntent {
    data class SelectMonth(val month: YearMonth) : HistoryIntent()
    data class SelectDate(val date: LocalDate) : HistoryIntent()
    object PreviousMonth : HistoryIntent()
    object NextMonth : HistoryIntent()
    object ClearSelectedDate : HistoryIntent()
    object RefreshData : HistoryIntent()
    object ClearError : HistoryIntent()
}