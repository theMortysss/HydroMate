package sdf.bitt.hydromate.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.repositories.DrinkRepository
import sdf.bitt.hydromate.domain.usecases.CalculateHydrationUseCase
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import sdf.bitt.hydromate.domain.usecases.GetWeeklyStatisticsUseCase
import sdf.bitt.hydromate.domain.usecases.TotalHydration
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getWeeklyStatisticsUseCase: GetWeeklyStatisticsUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val drinkRepository: DrinkRepository,
    private val calculateHydrationUseCase: CalculateHydrationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadWeeklyStatistics()
        observeSettings()
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

    private fun observeSettings() {
        viewModelScope.launch {
            getUserSettingsUseCase()
                .catch { }
                .collect { settings ->
                    _uiState.update {
                        it.copy(showNetHydration = settings.showNetHydration)
                    }
                    // ВАЖНО: При изменении настройки перезагружаем данные
                    loadWeeklyStatistics()
                }
        }
    }

    private fun loadWeeklyStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val weekStart = _uiState.value.selectedWeekStart

            combine(
                flow { emit(getWeeklyStatisticsUseCase(weekStart)) },
                drinkRepository.getAllActiveDrinks(),
                getUserSettingsUseCase()
            ) { statsResult, drinks, settings ->
                Triple(statsResult, drinks, settings)
            }.collect { (statsResult, drinks, settings) ->
                statsResult
                    .onSuccess { stats ->
                        // Рассчитываем гидратацию для всей недели
                        val allEntries = stats.dailyProgress.flatMap { it.entries }
                        val drinksMap = drinks.associateBy { it.id }

                        val hydrationData = if (allEntries.isNotEmpty()) {
                            calculateHydrationUseCase.calculateTotal(allEntries, drinksMap)
                        } else {
                            null
                        }

                        // FIXED: Обновляем каждый DailyProgress с учетом гидратации
                        val enhancedDailyProgress = stats.dailyProgress.map { dailyProgress ->
                            val dayEntries = dailyProgress.entries
                            val dayHydration = if (dayEntries.isNotEmpty()) {
                                calculateHydrationUseCase.calculateTotal(dayEntries, drinksMap)
                            } else {
                                null
                            }

                            dailyProgress.copy(
                                effectiveHydration = dayHydration?.totalEffective
                                    ?: dailyProgress.totalAmount,
                                netHydration = dayHydration?.netHydration
                                    ?: dailyProgress.totalAmount
                            )
                        }

                        // FIXED: Пересчитываем статистику с учетом showNetHydration
                        val totalForStats = if (settings.showNetHydration) {
                            enhancedDailyProgress.sumOf { it.netHydration }
                        } else {
                            enhancedDailyProgress.sumOf { it.totalAmount }
                        }

                        val averageDaily = totalForStats / 7

                        val daysGoalReached = enhancedDailyProgress.count { day ->
                            val currentAmount = if (settings.showNetHydration) {
                                day.netHydration
                            } else {
                                day.totalAmount
                            }
                            currentAmount >= day.goalAmount
                        }

                        // Пересчитываем streak с учетом настройки
                        val currentStreak = calculateStreakWithMode(
                            enhancedDailyProgress,
                            settings.showNetHydration
                        )

                        val enhancedStats = stats.copy(
                            dailyProgress = enhancedDailyProgress,
                            totalAmount = totalForStats,
                            averageDaily = averageDaily,
                            daysGoalReached = daysGoalReached,
                            currentStreak = currentStreak
                        )

                        _uiState.update {
                            it.copy(
                                weeklyStats = enhancedStats,
                                hydrationData = hydrationData,
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
    }

    private fun calculateStreakWithMode(
        dailyProgress: List<sdf.bitt.hydromate.domain.entities.DailyProgress>,
        showNetHydration: Boolean
    ): Int {
        var streak = 0
        for (progress in dailyProgress.reversed()) {
            val currentAmount = if (showNetHydration) {
                progress.netHydration
            } else {
                progress.totalAmount
            }

            if (currentAmount >= progress.goalAmount) {
                streak++
            } else {
                break
            }
        }
        return streak
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