package sdf.bitt.hydromate.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.repositories.DrinkRepository
import sdf.bitt.hydromate.domain.usecases.CalculateHydrationUseCase
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import sdf.bitt.hydromate.domain.usecases.GetWeeklyStatisticsUseCase
import sdf.bitt.hydromate.domain.usecases.TotalHydration
import java.time.LocalDate
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getWeeklyStatisticsUseCase: GetWeeklyStatisticsUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val drinkRepository: DrinkRepository,
    private val calculateHydrationUseCase: CalculateHydrationUseCase,
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
            _uiState.update { it.copy(isLoading = true) }

            combine(
                getWeeklyStatisticsUseCase(_uiState.value.selectedWeekStart),
                getUserSettingsUseCase(),
                drinkRepository.getAllActiveDrinks()
            ) { stats, settings, drinks ->
                Triple(stats, settings, drinks)
            }.catch { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load statistics"
                    )
                }
            }.collect { (stats, settings, drinks) ->
                val drinksMap = drinks.associateBy { it.id }

                // Рассчитываем гидратацию для всей недели
                val allEntries = stats.dailyProgress.flatMap { it.entries }
                val hydrationData = if (allEntries.isNotEmpty()) {
                    calculateHydrationUseCase.calculateTotal(allEntries, drinksMap)
                } else {
                    null
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
                        showNetHydration = settings.showNetHydration,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun calculateStreakWithMode(
        dailyProgress: List<DailyProgress>,
        showNetHydration: Boolean
    ): Int {
        if (dailyProgress.isEmpty()) return 0

        val today = LocalDate.now()
        val weekStart = dailyProgress.minOf { it.date }
        val weekEnd = dailyProgress.maxOf { it.date }
        val refDate = if (today.isBefore(weekEnd)) today else weekEnd

        val isCurrent = weekEnd >= today

        val refProgress = dailyProgress.find { it.date == refDate } ?: return 0
        val currentAmount = if (showNetHydration) {
            refProgress.netHydration
        } else {
            refProgress.totalAmount
        }
        val refMet = currentAmount >= refProgress.goalAmount

        // For current week, if today not met yet (ongoing day), count up to yesterday
        val countUpTo = if (isCurrent && !refMet) {
            refDate.minusDays(1)
        } else {
            refDate
        }

        val filteredInWeek = dailyProgress.filter { it.date <= countUpTo }.sortedByDescending { it.date }

        // Fetch drinks and settings once
        val settings = getUserSettingsUseCase().first()
        val drinks = drinkRepository.getAllActiveDrinks().first()
        val drinksMap = drinks.associateBy { it.id }

        var streak = 0
        var checkDate = countUpTo

        // First, count within the week
        for (progress in filteredInWeek) {
            val amt = if (showNetHydration) progress.netHydration else progress.totalAmount
            if (amt >= progress.goalAmount) {
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
                val amt = if (showNetHydration) progress.netHydration else progress.totalAmount
                if (amt >= progress.goalAmount) {
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