package sdf.bitt.hydromate.ui.screens.history

import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.UserSettings
import java.time.LocalDate
import java.time.YearMonth

data class HistoryUiState(
    val userSettings: UserSettings? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthlyProgress: Map<LocalDate, DailyProgress> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val selectedDateProgress: DailyProgress? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class HistoryIntent {
    data class SelectMonth(val month: YearMonth) : HistoryIntent()
    data class SelectDate(val date: LocalDate) : HistoryIntent()
    data class DeleteEntry(val entryId: Long) : HistoryIntent() // NEW
    object PreviousMonth : HistoryIntent()
    object NextMonth : HistoryIntent()
    object ClearSelectedDate : HistoryIntent()
    object RefreshData : HistoryIntent()
    object ClearError : HistoryIntent()
}

sealed class HistoryEffect {
    data class ShowError(val message: String) : HistoryEffect()
    data class ShowSuccess(val message: String) : HistoryEffect()
    object HapticFeedback : HistoryEffect()
}