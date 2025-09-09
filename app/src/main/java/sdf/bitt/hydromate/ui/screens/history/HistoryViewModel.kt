package sdf.bitt.hydromate.ui.screens.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.usecases.GetProgressForDateUseCase
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getProgressForDateUseCase: GetProgressForDateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadMonthlyData()
    }

    fun handleIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.SelectMonth -> selectMonth(intent.month)
            is HistoryIntent.SelectDate -> selectDate(intent.date)
            HistoryIntent.PreviousMonth -> navigateMonth(-1)
            HistoryIntent.NextMonth -> navigateMonth(1)
            HistoryIntent.ClearSelectedDate -> clearSelectedDate()
            HistoryIntent.RefreshData -> loadMonthlyData()
            HistoryIntent.ClearError -> clearError()
        }
    }

    private fun loadMonthlyData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentState = _uiState.value
                val month = currentState.selectedMonth
                val startDate = month.atDay(1)
                val endDate = month.atEndOfMonth()

                // Collect all flows for the month
                val flows = mutableListOf<Flow<Pair<LocalDate, DailyProgress>>>()

                var date = startDate
                while (!date.isAfter(endDate)) {
                    val currentDate = date // Capture current value
                    val progressFlow = getProgressForDateUseCase(currentDate)
                        .map { progress -> currentDate to progress }
                        .catch {
                            // In case of error for a specific day, emit empty progress
                            emit(currentDate to DailyProgress(
                                date = currentDate,
                                totalAmount = 0,
                                goalAmount = 2000,
                                entries = emptyList()
                            ))
                        }
                    flows.add(progressFlow)
                    date = date.plusDays(1)
                }

                // Combine all flows and collect first emission
                combine(flows) { progressArray ->
                    progressArray.toList()
                }.first().let { progressList ->
                    val monthlyProgress = progressList
                        .filter { (_, progress) -> progress.totalAmount > 0 }
                        .toMap()

                    _uiState.update {
                        it.copy(
                            monthlyProgress = monthlyProgress,
                            isLoading = false
                        )
                    }
                }

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load history"
                    )
                }
            }
        }
    }

    private fun selectMonth(month: YearMonth) {
        _uiState.update {
            it.copy(
                selectedMonth = month,
                selectedDate = null,
                selectedDateProgress = null
            )
        }
        loadMonthlyData()
    }

    private fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            val progress = _uiState.value.monthlyProgress[date]
            _uiState.update {
                it.copy(
                    selectedDate = date,
                    selectedDateProgress = progress
                )
            }
        }
    }

    private fun navigateMonth(monthOffset: Int) {
        val newMonth = _uiState.value.selectedMonth.plusMonths(monthOffset.toLong())
        selectMonth(newMonth)
    }

    private fun clearSelectedDate() {
        _uiState.update {
            it.copy(
                selectedDate = null,
                selectedDateProgress = null
            )
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}