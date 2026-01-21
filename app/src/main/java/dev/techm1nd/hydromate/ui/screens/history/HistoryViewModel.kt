package dev.techm1nd.hydromate.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.repositories.DrinkRepository
import dev.techm1nd.hydromate.domain.usecases.hydration.AddWaterEntryForDateUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.CalculateHydrationUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.CheckGoalReachedUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.DeleteWaterEntryUseCase
import dev.techm1nd.hydromate.domain.usecases.stat.GetProgressForDateUseCase
import dev.techm1nd.hydromate.domain.usecases.setting.GetUserSettingsUseCase
import dev.techm1nd.hydromate.ui.notification.NotificationScheduler
import dev.techm1nd.hydromate.ui.screens.history.model.HistoryEffect
import dev.techm1nd.hydromate.ui.screens.history.model.HistoryIntent
import dev.techm1nd.hydromate.ui.screens.history.model.HistoryState
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarController
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
    private val addWaterEntryForDateUseCase: AddWaterEntryForDateUseCase,
    private val globalSnackbarController: GlobalSnackbarController
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

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
            is HistoryIntent.CreateCustomDrink -> createCustomDrink(intent.drink)
        }
    }

    private fun observeDrinks() {
        viewModelScope.launch {
            drinkRepository.getAllActiveDrinks()
                .catch { }
                .collect { drinks ->
                    _state.update { it.copy(drinks = drinks) }
                }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getUserSettingsUseCase()
                .catch { }
                .collect { settings ->
                    _state.update {
                        it.copy(userSettings = settings)
                    }
                    // При изменении настроек перезагружаем данные
                    loadMonthlyData()
                }
        }
    }

    private fun loadMonthlyData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val currentState = _state.value
                val month = currentState.selectedMonth
                val startDate = month.atDay(1)
                val endDate = month.atEndOfMonth()

                // Получаем напитки
                val drinks = drinkRepository.getAllActiveDrinks().first()
                val drinksMap = drinks.associateBy { it.id }

                // Собираем flows для всех дней месяца и подписываемся на изменения
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

                // FIXED: Используем combine для реактивного обновления
                combine(flows) { progressArray ->
                    progressArray.toList()
                }.collect { progressList ->
                    val monthlyProgress = progressList
                        .filter { (_, progress) -> progress.totalAmount > 0 }
                        .toMap()

                    _state.update {
                        it.copy(
                            monthlyProgress = monthlyProgress,
                            isLoading = false
                        )
                    }

                    // Обновляем selectedDateProgress если дата выбрана
                    val selectedDate = _state.value.selectedDate
                    if (selectedDate != null) {
                        _state.update {
                            it.copy(selectedDateProgress = monthlyProgress[selectedDate])
                        }
                    }
                }

            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load history"
                    )
                }
            }
        }
    }

    private fun showAddWaterDialog(date: LocalDate) {
        _state.update {
            it.copy(
                showAddWaterDialog = true,
                dateForNewEntry = date
            )
        }
    }

    private fun hideAddWaterDialog() {
        _state.update {
            it.copy(
                showAddWaterDialog = false,
                dateForNewEntry = null
            )
        }
    }

    private fun selectMonth(month: YearMonth) {
        _state.update {
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
            val progress = _state.value.monthlyProgress[date]
            _state.update {
                it.copy(
                    selectedDate = date,
                    selectedDateProgress = progress
                )
            }
        }
    }

    private fun navigateMonth(monthOffset: Int) {
        val newMonth = _state.value.selectedMonth.plusMonths(monthOffset.toLong())
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
                    globalSnackbarController.showSuccess(
                        "Added ${amount}ml of ${drink.name} for ${date.format(
                            DateTimeFormatter.ofPattern("MMM dd")
                        )}"
                    )
                    hideAddWaterDialog()
                    loadMonthlyData() // Перезагружаем данные
                }
                .onFailure { exception ->
                    globalSnackbarController.showError(
                        exception.message ?: "Failed to add water entry"
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
                    globalSnackbarController.showMessage("Entry deleted")
                }
                .onFailure { exception ->
                    globalSnackbarController.showError(
                        exception.message ?: "Failed to delete entry"
                    )
                }
        }
    }

    private suspend fun checkAndHandleGoalAchievement() {
        val settings = _state.value.userSettings ?: return
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

    private fun createCustomDrink(drink: Drink) {
        viewModelScope.launch {
            drinkRepository.createCustomDrink(drink)
                .onSuccess { drinkId ->
                    globalSnackbarController.showSuccess("Custom drink \"${drink.name}\" created!")
                }
                .onFailure { exception ->
                    globalSnackbarController.showError(exception.message ?: "Failed to create custom drink")
                }
        }
    }

    private fun clearSelectedDate() {
        _state.update {
            it.copy(
                selectedDate = null,
                selectedDateProgress = null
            )
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
}