package sdf.bitt.hydromate.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.repositories.DrinkRepository
import sdf.bitt.hydromate.domain.usecases.AddWaterEntryForDateUseCase
import sdf.bitt.hydromate.domain.usecases.CalculateHydrationUseCase
import sdf.bitt.hydromate.domain.usecases.CheckGoalReachedUseCase
import sdf.bitt.hydromate.domain.usecases.DeleteWaterEntryUseCase
import sdf.bitt.hydromate.domain.usecases.GetProgressForDateUseCase
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import sdf.bitt.hydromate.ui.notification.NotificationScheduler
import sdf.bitt.hydromate.ui.screens.home.HomeEffect
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getProgressForDateUseCase: GetProgressForDateUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val drinkRepository: DrinkRepository,
    private val calculateHydrationUseCase: CalculateHydrationUseCase,
    private val deleteWaterEntryUseCase: DeleteWaterEntryUseCase,
    private val checkGoalReachedUseCase: CheckGoalReachedUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val addWaterEntryForDateUseCase: AddWaterEntryForDateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _effects = Channel<HistoryEffect>(Channel.BUFFERED)
    val effects: Flow<HistoryEffect> = _effects.receiveAsFlow()

    init {
        observeSettings()
        loadMonthlyData()
        observeDrinks()
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
            is HistoryIntent.DeleteEntry -> deleteEntry(intent.entryId)
            is HistoryIntent.ShowAddWaterDialog -> showAddWaterDialog(intent.date)
            HistoryIntent.HideAddWaterDialog -> hideAddWaterDialog()
            is HistoryIntent.AddWaterForDate -> addWaterForDate(
                intent.date, intent.amount, intent.drink, intent.time
            )
        }
    }

    private fun observeDrinks() {
        viewModelScope.launch {
            drinkRepository.getAllActiveDrinks()
                .catch { }
                .collect { drinks ->
                    _uiState.update { it.copy(drinks = drinks) }
                }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getUserSettingsUseCase()
                .catch { }
                .collect { settings ->
                    _uiState.update {
                        it.copy(userSettings = settings)
                    }
                }
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

                // Получаем напитки
                val drinks = drinkRepository.getAllActiveDrinks().first()
                val drinksMap = drinks.associateBy { it.id }

                // Collect all flows for the month
                val flows = mutableListOf<Flow<Pair<LocalDate, DailyProgress>>>()

                var date = startDate
                while (!date.isAfter(endDate)) {
                    val currentDate = date
                    val progressFlow = getProgressForDateUseCase(currentDate)
                        .map { progress ->
                            // Рассчитываем гидратацию для каждого дня
                            val hydration = if (progress.entries.isNotEmpty()) {
                                calculateHydrationUseCase.calculateTotal(
                                    progress.entries,
                                    drinksMap
                                )
                            } else {
                                null
                            }

                            val enhancedProgress = progress.copy(
                                effectiveHydration = hydration?.totalEffective
                                    ?: progress.totalAmount,
                                netHydration = hydration?.netHydration ?: progress.totalAmount
                            )

                            currentDate to enhancedProgress
                        }
                        .catch {
                            emit(
                                currentDate to DailyProgress(
                                    date = currentDate,
                                    totalAmount = 0,
                                    goalAmount = 2000,
                                    entries = emptyList()
                                )
                            )
                        }
                    flows.add(progressFlow)
                    date = date.plusDays(1)
                }

                // Combine all flows
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

    private fun showAddWaterDialog(date: LocalDate) {
        _uiState.update {
            it.copy(
                showAddWaterDialog = true,
                dateForNewEntry = date
            )
        }
    }

    private fun hideAddWaterDialog() {
        _uiState.update {
            it.copy(
                showAddWaterDialog = false,
                dateForNewEntry = null
            )
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

    private fun addWaterForDate(
        date: LocalDate,
        amount: Int,
        drink: Drink,
        time: LocalDateTime
    ) {
        viewModelScope.launch {
            // Создаем запись с указанной датой
            addWaterEntryForDateUseCase(amount, drink, time)
                .onSuccess {
                    _effects.trySend(
                        HistoryEffect.ShowSuccess(
                            "Added ${amount}ml of ${drink.name} for ${date.format(
                                DateTimeFormatter.ofPattern("MMM dd")
                            )}"
                        )
                    )
                    hideAddWaterDialog()
                    loadMonthlyData() // Перезагружаем данные
                }
                .onFailure { exception ->
                    _effects.trySend(
                        HistoryEffect.ShowError(
                            exception.message ?: "Failed to add water entry"
                        )
                    )
                }
        }
    }

    private fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            deleteWaterEntryUseCase(entryId)
                .onSuccess {
                    // После удаления проверяем, возможно цель больше не достигнута
                    checkAndHandleGoalAchievement()
                }
                .onFailure { exception ->
                    _effects.trySend(
                        HistoryEffect.ShowError(
                            exception.message ?: "Failed to delete entry"
                        )
                    )
                }
        }
    }

    private suspend fun checkAndHandleGoalAchievement() {
        val settings = _uiState.value.userSettings ?: return
        if (!settings.notificationsEnabled) return

        checkGoalReachedUseCase()
            .onSuccess { isGoalReached ->
                if (isGoalReached) {
                    // Цель достигнута - планируем напоминания на завтра
                    notificationScheduler.scheduleNextDayReminder(settings)
                } else {
                    // Цель не достигнута - продолжаем обычное расписание
                    notificationScheduler.scheduleNotifications(settings)
                }
            }
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