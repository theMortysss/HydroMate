package dev.techm1nd.hydromate.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.entities.QuickAddPreset
import dev.techm1nd.hydromate.domain.entities.UserProfile
import dev.techm1nd.hydromate.domain.entities.UserSettings
import dev.techm1nd.hydromate.domain.repositories.DrinkRepository
import dev.techm1nd.hydromate.domain.repositories.TipsRepository
import dev.techm1nd.hydromate.domain.usecases.hydration.AddWaterEntryUseCase
import dev.techm1nd.hydromate.domain.usecases.character.CalculateCharacterStateUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.CalculateHydrationUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.CheckGoalReachedUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.DeleteWaterEntryUseCase
import dev.techm1nd.hydromate.domain.usecases.stat.GetTodayProgressUseCase
import dev.techm1nd.hydromate.domain.usecases.setting.GetUserSettingsUseCase
import dev.techm1nd.hydromate.domain.usecases.setting.UpdateUserSettingsUseCase
import dev.techm1nd.hydromate.domain.usecases.achievement.CheckAchievementProgressUseCase
import dev.techm1nd.hydromate.domain.usecases.challenge.UpdateChallengeProgressUseCase
import dev.techm1nd.hydromate.domain.usecases.challenge.UpdateChallengeStreaksUseCase
import dev.techm1nd.hydromate.domain.usecases.profile.AddXPUseCase
import dev.techm1nd.hydromate.domain.usecases.profile.GetUserProfileUseCase
import dev.techm1nd.hydromate.ui.notification.NotificationScheduler
import dev.techm1nd.hydromate.ui.screens.home.model.HomeEffect
import dev.techm1nd.hydromate.ui.screens.home.model.HomeIntent
import dev.techm1nd.hydromate.ui.screens.home.model.HomeState
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarController
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val addWaterEntryUseCase: AddWaterEntryUseCase,
    private val getTodayProgressUseCase: GetTodayProgressUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val deleteWaterEntryUseCase: DeleteWaterEntryUseCase,
    private val calculateCharacterStateUseCase: CalculateCharacterStateUseCase,
    private val calculateHydrationUseCase: CalculateHydrationUseCase,
    private val drinkRepository: DrinkRepository,
    private val checkGoalReachedUseCase: CheckGoalReachedUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    private val updateChallengeProgressUseCase: UpdateChallengeProgressUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val checkAchievementProgressUseCase: CheckAchievementProgressUseCase,
    private val updateChallengeStreaksUseCase: UpdateChallengeStreaksUseCase,
    private val addXPUseCase: AddXPUseCase,
    private val tipsRepository: TipsRepository,
    private val globalSnackbarController: GlobalSnackbarController
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    init {
        observeData()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.AddWater -> addWater(intent.amount, intent.drink, intent.timestamp)
            is HomeIntent.DeleteEntry -> deleteEntry(intent.entryId)
            is HomeIntent.SelectDrink -> selectDrink(intent.drink)
            is HomeIntent.CreateCustomDrink -> createCustomDrink(intent.drink)
            is HomeIntent.UpdateQuickPresets -> updateQuickPresets(intent.presets)
            is HomeIntent.MarkTipAsViewed -> markTipAsViewed(intent.tipId)
            HomeIntent.RefreshData -> refreshData()
            HomeIntent.ClearError -> clearError()
        }
    }

    private fun markTipAsViewed(tipId: String) {
        viewModelScope.launch {
            tipsRepository.markTipAsViewed(tipId)
        }
    }

    private fun updateQuickPresets(presets: List<QuickAddPreset>) {
        viewModelScope.launch {
            val currentSettings = _state.value.userSettings ?: return@launch
            val newSettings = currentSettings.copy(quickAddPresets = presets)

            updateUserSettingsUseCase(newSettings)
                .onSuccess {
                    globalSnackbarController.showSuccess("Quick presets updated successfully!")
                }
                .onFailure { exception ->
                    globalSnackbarController.showError(
                        exception.message ?: "Failed to update quick presets"
                    )
                }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            combine(
                getTodayProgressUseCase(),
                getUserSettingsUseCase(),
                drinkRepository.getAllActiveDrinks(),
                getUserProfileUseCase(),
                tipsRepository.getViewedTipIds()
            ) { progress, settings, drinks, profile, viewedTips ->
                CombinedData(progress, settings, drinks, profile, viewedTips)
            }.catch { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            }.collect { (progress, settings, drinks, profile, viewedTips) ->
                // Расчет гидратации
                val drinksMap = drinks.associateBy { it.id }
                val totalHydration = calculateHydrationUseCase.calculateTotal(
                    progress.entries,
                    drinksMap
                )

                val currentHydration = totalHydration.netHydration

                val hydrationProgress = calculateHydrationUseCase.calculateProgress(
                    netHydration = currentHydration,
                    dailyGoal = settings.dailyGoal,
                )

                val enhancedProgress = progress.copy(
                    effectiveHydration = totalHydration.totalEffective,
                    netHydration = totalHydration.netHydration
                )

                val progressForCharacter = enhancedProgress.copy(
                    totalAmount = currentHydration
                )
                val characterState = calculateCharacterStateUseCase(progressForCharacter)

                val previousGoalReached = _state.value.hydrationProgress?.isGoalReached ?: false
                val currentGoalReached = hydrationProgress.isGoalReached

                _state.update {
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
                        selectedCharacter = profile.selectedCharacter,
                        viewedTipIds = viewedTips,
                        isLoading = false
                    )
                }

                // Показать празднование достижения цели
                if (!previousGoalReached && currentGoalReached) {
                    globalSnackbarController.showGoalReached()

                    // НОВАЯ ЛОГИКА: Автоматически планируем напоминания на следующий день
                    if (settings.notificationsEnabled) {
                        notificationScheduler.scheduleNextDayReminder(settings)
                    }
                }
            }
        }
    }

    private fun addWater(amount: Int, drink: Drink, timestamp: LocalDateTime) {
        viewModelScope.launch {
            _state.update { it.copy(isAddingWater = true) }

            val hydrationResult = calculateHydrationUseCase(amount, drink)

            addWaterEntryUseCase(amount, drink, timestamp)
                .onSuccess {
                    _effects.trySend(HomeEffect.ShowAddWaterAnimation)
                    _effects.trySend(HomeEffect.HapticFeedback)

                    // NEW: Check challenge violations
                    updateChallengeProgressUseCase(drink)
                        .onSuccess { violatedChallengeIds ->
                            if (violatedChallengeIds.isNotEmpty()) {
                                globalSnackbarController.showChallengeViolation(
                                    challengeName = "Active Challenge",
                                    drinkName = drink.name
                                )
                            }
                        }

                    // NEW: Track drink in profile
                    addXPUseCase.incrementDrinkCount(drink.name)

                    // Check achievements
                    checkAchievements()

                    _effects.trySend(
                        HomeEffect.ShowHydrationInfo(
                            actualAmount = hydrationResult.actualAmount,
                            effectiveAmount = hydrationResult.effectiveAmount,
                            netHydration = hydrationResult.netHydration,
                            drink = drink
                        )
                    )

                    checkAndHandleGoalAchievement()

                    // NEW: Update challenge streaks
                    updateChallengeStreaksUseCase()
                }
                .onFailure { exception ->
                    globalSnackbarController.showError(
                        exception.message ?: "Failed to add water entry"
                    )
                }

            _state.update { it.copy(isAddingWater = false) }
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
                    globalSnackbarController.showError(
                        exception.message ?: "Failed to delete entry"
                    )
                }
        }
    }

    private suspend fun checkAchievements() {
        checkAchievementProgressUseCase()
            .onSuccess { newlyUnlocked ->
                newlyUnlocked.forEach { achievement ->
                    globalSnackbarController.showAchievement(
                        title = achievement.title,
                        description = achievement.description
                    )

                    achievement.unlockableCharacter?.let { character ->
                        globalSnackbarController.showCharacterUnlocked(character.displayName)
                    }
                }
            }
    }

    /**
     * Проверяет достижение цели и обновляет расписание уведомлений
     */
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

    private fun selectDrink(drink: Drink) {
        _state.update { it.copy(selectedDrink = drink) }
    }

    private fun createCustomDrink(drink: Drink) {
        viewModelScope.launch {
            drinkRepository.createCustomDrink(drink)
                .onSuccess { drinkId ->
                    globalSnackbarController.showSuccess(
                        "Custom drink \"${drink.name}\" created!"
                    )

                    val createdDrink = drink.copy(id = drinkId)
                    _state.update { it.copy(selectedDrink = createdDrink) }
                }
                .onFailure { exception ->
                    globalSnackbarController.showError(
                        exception.message ?: "Failed to create custom drink"
                    )
                }
        }
    }

    private fun refreshData() {
        // Данные автоматически обновляются через Flow
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

private data class CombinedData(
    val progress: DailyProgress,
    val settings: UserSettings,
    val drinks: List<Drink>,
    val profile: UserProfile,
    val viewedTips: Set<String>
)