package sdf.bitt.hydromate.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.usecases.GetWeeklyStatisticsUseCase
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getWeeklyStatisticsUseCase: GetWeeklyStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadWeeklyStatistics()
    }

    fun handleIntent(intent: StatisticsIntent) {
        when (intent) {
            StatisticsIntent.RefreshData -> loadWeeklyStatistics()
            is StatisticsIntent.SelectWeek -> selectWeek(intent.weekStart)
            StatisticsIntent.PreviousWeek -> navigateWeek(-7)
            StatisticsIntent.NextWeek -> navigateWeek(7)
            StatisticsIntent.ClearError -> clearError()
        }
    }

    private fun loadWeeklyStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getWeeklyStatisticsUseCase(_uiState.value.selectedWeekStart)
                .onSuccess { stats ->
                    _uiState.update {
                        it.copy(
                            weeklyStats = stats,
                            isLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load statistics"
                        )
                    }
                }
        }
    }

    private fun selectWeek(weekStart: LocalDate) {
        _uiState.update { it.copy(selectedWeekStart = weekStart) }
        loadWeeklyStatistics()
    }

    private fun navigateWeek(days: Long) {
        val newWeekStart = _uiState.value.selectedWeekStart.plusDays(days)
        selectWeek(newWeekStart)
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
