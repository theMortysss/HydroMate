package sdf.bitt.hydromate.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.repositories.DrinkRepository
import sdf.bitt.hydromate.domain.usecases.AddWaterEntryUseCase
import sdf.bitt.hydromate.domain.usecases.CalculateCharacterStateUseCase
import sdf.bitt.hydromate.domain.usecases.CalculateHydrationUseCase
import sdf.bitt.hydromate.domain.usecases.DeleteWaterEntryUseCase
import sdf.bitt.hydromate.domain.usecases.GetTodayProgressUseCase
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val addWaterEntryUseCase: AddWaterEntryUseCase,
    private val getTodayProgressUseCase: GetTodayProgressUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val deleteWaterEntryUseCase: DeleteWaterEntryUseCase,
    private val calculateCharacterStateUseCase: CalculateCharacterStateUseCase,
    private val calculateHydrationUseCase: CalculateHydrationUseCase,
    private val drinkRepository: DrinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    init {
        observeData()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.AddWater -> addWater(intent.amount, intent.drink)
            is HomeIntent.DeleteEntry -> deleteEntry(intent.entryId)
            is HomeIntent.SelectDrink -> selectDrink(intent.drink)
            is HomeIntent.CreateCustomDrink -> createCustomDrink(intent.drink)
            HomeIntent.RefreshData -> refreshData()
            HomeIntent.ClearError -> clearError()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                getTodayProgressUseCase(),
                getUserSettingsUseCase(),
                drinkRepository.getAllActiveDrinks()
            ) { progress, settings, drinks ->
                Triple(progress, settings, drinks)
            }.catch { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            }.collect { (progress, settings, drinks) ->
                // Расчет гидратации
                val drinksMap = drinks.associateBy { it.id }
                val totalHydration = calculateHydrationUseCase.calculateTotal(
                    progress.entries,
                    drinksMap
                )

                // FIXED: Используем showNetHydration для расчета прогресса
                val currentHydration = if (settings.showNetHydration) {
                    totalHydration.netHydration
                } else {
                    totalHydration.totalActual
                }

                val hydrationProgress = calculateHydrationUseCase.calculateProgress(
                    netHydration = currentHydration,
                    dailyGoal = settings.dailyGoal,
                    hydrationThreshold = settings.hydrationThreshold
                )

                // Обновляем progress с данными гидратации
                val enhancedProgress = progress.copy(
                    effectiveHydration = totalHydration.totalEffective,
                    netHydration = totalHydration.netHydration
                )

                // Расчет состояния персонажа ТАКЖЕ на основе showNetHydration
                val progressForCharacter = enhancedProgress.copy(
                    totalAmount = currentHydration
                )
                val characterState = calculateCharacterStateUseCase(progressForCharacter)

                val previousGoalReached = _uiState.value.hydrationProgress?.isGoalReached == true
                val currentGoalReached = hydrationProgress.isGoalReached

                _uiState.update {
                    it.copy(
                        todayProgress = enhancedProgress,
                        userSettings = settings,
                        characterState = characterState,
                        totalHydration = totalHydration,
                        hydrationProgress = hydrationProgress,
                        drinks = drinks,
                        selectedDrink = it.selectedDrink
                            ?: drinks.firstOrNull { drink -> drink.id == 1L }
                            ?: Drink.WATER,
                        isLoading = false
                    )
                }

                // Показать празднование достижения цели
                if (!previousGoalReached && currentGoalReached) {
                    _effects.trySend(HomeEffect.ShowGoalReachedCelebration)
                }
            }
        }
    }

    private fun addWater(amount: Int, drink: Drink) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingWater = true) }

            val hydrationResult = calculateHydrationUseCase(amount, drink)

            addWaterEntryUseCase(amount, drink)
                .onSuccess {
                    _effects.trySend(HomeEffect.ShowAddWaterAnimation)
                    _effects.trySend(HomeEffect.HapticFeedback)

                    _effects.trySend(
                        HomeEffect.ShowHydrationInfo(
                            actualAmount = hydrationResult.actualAmount,
                            effectiveAmount = hydrationResult.effectiveAmount,
                            netHydration = hydrationResult.netHydration,
                            drink = drink
                        )
                    )

                    if (hydrationResult.dehydrationAmount > 0) {
                        _effects.trySend(
                            HomeEffect.ShowSuccess(
                                "Added ${amount}ml of ${drink.icon} ${drink.name}\n" +
                                        "Net hydration: ${hydrationResult.netHydration}ml " +
                                        "(${hydrationResult.dehydrationAmount}ml dehydration effect)"
                            )
                        )
                    }
                }
                .onFailure { exception ->
                    _effects.trySend(
                        HomeEffect.ShowError(
                            exception.message ?: "Failed to add water entry"
                        )
                    )
                }

            _uiState.update { it.copy(isAddingWater = false) }
        }
    }

    private fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            deleteWaterEntryUseCase(entryId)
                .onFailure { exception ->
                    _effects.trySend(
                        HomeEffect.ShowError(
                            exception.message ?: "Failed to delete entry"
                        )
                    )
                }
        }
    }

    private fun selectDrink(drink: Drink) {
        _uiState.update { it.copy(selectedDrink = drink) }
    }

    private fun createCustomDrink(drink: Drink) {
        viewModelScope.launch {
            drinkRepository.createCustomDrink(drink)
                .onSuccess { drinkId ->
                    _effects.trySend(
                        HomeEffect.ShowSuccess("Custom drink \"${drink.name}\" created!")
                    )

                    val createdDrink = drink.copy(id = drinkId)
                    _uiState.update { it.copy(selectedDrink = createdDrink) }
                }
                .onFailure { exception ->
                    _effects.trySend(
                        HomeEffect.ShowError(
                            exception.message ?: "Failed to create custom drink"
                        )
                    )
                }
        }
    }

    private fun refreshData() {
        // Данные автоматически обновляются через Flow
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}