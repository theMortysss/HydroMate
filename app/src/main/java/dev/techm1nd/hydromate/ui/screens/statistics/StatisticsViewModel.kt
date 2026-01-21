package dev.techm1nd.hydromate.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.repositories.DrinkRepository
import dev.techm1nd.hydromate.domain.usecases.hydration.CalculateHydrationUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.TotalHydration
import dev.techm1nd.hydromate.domain.usecases.setting.GetUserSettingsUseCase
import dev.techm1nd.hydromate.domain.usecases.stat.GetWeeklyStatisticsUseCase
import dev.techm1nd.hydromate.ui.screens.statistics.model.StatisticsIntent
import dev.techm1nd.hydromate.ui.screens.statistics.model.StatisticsState
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarController
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getWeeklyStatisticsUseCase: GetWeeklyStatisticsUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val drinkRepository: DrinkRepository,
    private val calculateHydrationUseCase: CalculateHydrationUseCase,
    private val globalSnackbarController: GlobalSnackbarController
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

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
            _state.update { it.copy(isLoading = true) }

            combine(
                getWeeklyStatisticsUseCase(_state.value.selectedWeekStart),
                drinkRepository.getAllActiveDrinks()
            ) { stats, drinks ->
                Pair(stats, drinks)
            }.catch { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load statistics"
                    )
                }
                globalSnackbarController.showError(exception.message ?: "Failed to load statistics")
            }.collect { (stats, drinks) ->
                val drinksMap = drinks.associateBy { it.id }

                // Рассчитываем гидратацию для всей недели
                val allEntries = stats.dailyProgress.flatMap { it.entries }
                val hydrationData = if (allEntries.isNotEmpty()) {
                    calculateHydrationUseCase.calculateTotal(allEntries, drinksMap)
                } else {
                    TotalHydration()
                }

                // Обновляем каждый DailyProgress с учетом гидратации
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

                // Пересчитываем статистику с учетом showNetHydration
                val totalForStats = enhancedDailyProgress.sumOf { it.netHydration }

                val averageDaily = totalForStats / 7

                val daysGoalReached = enhancedDailyProgress.count { day ->
                    day.netHydration >= day.goalAmount
                }

                val currentStreak = calculateStreakWithMode(
                    enhancedDailyProgress,
                )

                val enhancedStats = stats.copy(
                    dailyProgress = enhancedDailyProgress,
                    totalAmount = totalForStats,
                    averageDaily = averageDaily,
                    daysGoalReached = daysGoalReached,
                    currentStreak = currentStreak
                )

                _state.update {
                    it.copy(
                        weeklyStats = enhancedStats,
                        hydrationData = hydrationData,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun calculateStreakWithMode(
        dailyProgress: List<DailyProgress>,
    ): Int {
        if (dailyProgress.isEmpty()) return 0

        val today = LocalDate.now()
        val weekStart = dailyProgress.minOf { it.date }
        val weekEnd = dailyProgress.maxOf { it.date }
        val refDate = if (today.isBefore(weekEnd)) today else weekEnd

        val isCurrent = weekEnd >= today

        val refProgress = dailyProgress.find { it.date == refDate } ?: return 0
        val currentAmount = refProgress.netHydration
        val refMet = currentAmount >= refProgress.goalAmount

        // For current week, if today not met yet (ongoing day), count up to yesterday
        val countUpTo = if (isCurrent && !refMet) {
            refDate.minusDays(1)
        } else {
            refDate
        }

        val filteredInWeek = dailyProgress.filter { it.date <= countUpTo }.sortedByDescending { it.date }

        // Fetch drinks and settings once
        val drinks = drinkRepository.getAllActiveDrinks().first()
        val drinksMap = drinks.associateBy { it.id }

        var streak = 0
        var checkDate = countUpTo

        // First, count within the week
        for (progress in filteredInWeek) {
            if (progress.netHydration >= progress.goalAmount) {
                streak++
                checkDate = progress.date.minusDays(1)
            } else {
                return streak
            }
        }

        // If streak reached the start of the week without breaking, continue backward by loading previous weeks
        var currentWeekStart = weekStart
        while (true) {
            val prevWeekStart = currentWeekStart.minusDays(7)
            val prevStats = getWeeklyStatisticsUseCase(prevWeekStart).first()
            if (prevStats.dailyProgress.isEmpty()) break

            val prevEnhancedDailyProgress = prevStats.dailyProgress.map { dailyProgress ->
                val dayEntries = dailyProgress.entries
                val dayHydration = if (dayEntries.isNotEmpty()) {
                    calculateHydrationUseCase.calculateTotal(dayEntries, drinksMap)
                } else {
                    null
                }

                dailyProgress.copy(
                    effectiveHydration = dayHydration?.totalEffective ?: dailyProgress.totalAmount,
                    netHydration = dayHydration?.netHydration ?: dailyProgress.totalAmount
                )
            }

            val sortedPrev = prevEnhancedDailyProgress.sortedByDescending { it.date }

            var continued = true
            for (progress in sortedPrev) {
                if (progress.netHydration >= progress.goalAmount) {
                    streak++
                } else {
                    continued = false
                    break
                }
            }

            if (!continued) break

            currentWeekStart = prevWeekStart
        }

        return streak
    }

    private fun selectWeek(weekStart: LocalDate) {
        _state.update { it.copy(selectedWeekStart = weekStart) }
        loadWeeklyStatistics()
    }

    private fun navigateWeek(days: Long) {
        val newWeekStart = _state.value.selectedWeekStart.plusDays(days)
        selectWeek(newWeekStart)
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
}