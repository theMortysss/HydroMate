package dev.techm1nd.hydromate.ui.screens.history

import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.entities.UserSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

data class HistoryUiState(
    val drinks: List<Drink> = emptyList(),
    val userSettings: UserSettings? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthlyProgress: Map<LocalDate, DailyProgress> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val selectedDateProgress: DailyProgress? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddWaterDialog: Boolean = false,
    val dateForNewEntry: LocalDate? = null,
)

sealed class HistoryIntent {
    data class SelectMonth(val month: YearMonth) : HistoryIntent()
    data class SelectDate(val date: LocalDate) : HistoryIntent()
    data class DeleteEntry(val entryId: Long) : HistoryIntent()
    object PreviousMonth : HistoryIntent()
    object NextMonth : HistoryIntent()
    object ClearSelectedDate : HistoryIntent()
    object RefreshData : HistoryIntent()
    object ClearError : HistoryIntent()
    data class ShowAddWaterDialog(val date: LocalDate) : HistoryIntent()
    object HideAddWaterDialog : HistoryIntent()
    data class CreateCustomDrink(val drink: Drink) : HistoryIntent()
    data class AddWaterForDate(
        val date: LocalDate,
        val amount: Int,
        val drink: Drink,
        val time: LocalDateTime
    ) : HistoryIntent()
}

sealed class HistoryEffect {
    data class ShowError(val message: String) : HistoryEffect()
    data class ShowSuccess(val message: String) : HistoryEffect()
    object HapticFeedback : HistoryEffect()
}