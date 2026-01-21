package dev.techm1nd.hydromate.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.entities.QuickAddPreset
import dev.techm1nd.hydromate.domain.entities.UserProfile
import dev.techm1nd.hydromate.domain.entities.UserSettings
import dev.techm1nd.hydromate.domain.repositories.DrinkRepository
import dev.techm1nd.hydromate.domain.usecases.hydration.CalculateRecommendedGoalUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.RecommendedGoalResult
import dev.techm1nd.hydromate.domain.usecases.setting.GetUserSettingsUseCase
import dev.techm1nd.hydromate.domain.usecases.stat.UpdateDailyGoalUseCase
import dev.techm1nd.hydromate.domain.usecases.setting.UpdateUserSettingsUseCase
import dev.techm1nd.hydromate.domain.usecases.profile.GetUserProfileUseCase
import dev.techm1nd.hydromate.ui.notification.NotificationScheduler
import dev.techm1nd.hydromate.ui.screens.settings.model.SettingsIntent
import dev.techm1nd.hydromate.ui.screens.settings.model.SettingsState
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarController
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    private val updateDailyGoalUseCase: UpdateDailyGoalUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val drinkRepository: DrinkRepository,
    private val calculateRecommendedGoalUseCase: CalculateRecommendedGoalUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val globalSnackbarController: GlobalSnackbarController
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observeSettings()
    }

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateDailyGoal -> updateDailyGoal(intent.goal)
            is SettingsIntent.UpdateWakeUpTime -> updateWakeUpTime(intent.time)
            is SettingsIntent.UpdateBedTime -> updateBedTime(intent.time)
            is SettingsIntent.UpdateQuickAmounts -> updateQuickAmounts(intent.amounts)
            is SettingsIntent.UpdateSettings -> updateSettings(intent.settings)

            SettingsIntent.ShowGoalDialog -> _state.update { it.copy(showGoalDialog = true) }
            SettingsIntent.HideGoalDialog -> _state.update { it.copy(showGoalDialog = false) }
            is SettingsIntent.ShowTimePickerDialog -> _state.update {
                it.copy(showTimePickerDialog = true, timePickerType = intent.type)
            }

            SettingsIntent.HideTimePickerDialog -> _state.update { it.copy(showTimePickerDialog = false) }

            SettingsIntent.RefreshSettings -> observeSettings()
            SettingsIntent.ClearError -> _state.update { it.copy(error = null) }

            // Обработка настроек гидратации
            is SettingsIntent.CalculateRecommendedGoal -> calculateRecommendedGoalForProfile(intent.profile)
            SettingsIntent.HideProfileDialog -> _state.update { it.copy(showProfileDialog = false) }
            SettingsIntent.ShowProfileDialog -> _state.update { it.copy(showProfileDialog = true) }
            is SettingsIntent.UpdateProfile -> updateProfile(intent.profile)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            combine(
                getUserSettingsUseCase(),
                drinkRepository.getAllActiveDrinks(),
                getUserProfileUseCase()
            ) { settings, drinks, profile ->
                Triple(settings, drinks, profile)
            }.catch { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load settings"
                    )
                }
                globalSnackbarController.showError(exception.message ?: "Failed to load settings")
            }.collect { (settings, drinks, profile) ->
                _state.update {
                    it.copy(
                        drinks = drinks,
                        settings = settings,
                        recommendedGoal = if (!settings.profile.isManualGoal) calculateRecommendedGoalUseCase(
                            settings.profile
                        ) else RecommendedGoalResult(),
                        selectedCharacter = profile.selectedCharacter,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            // 1. Рассчитываем новую рекомендуемую норму
            val recommendedResult = calculateRecommendedGoalUseCase(profile)

            // 2. Обновляем settings с новым профилем
            val newSettings = _state.value.settings.copy(
                profile = profile,
                // Если не ручной режим - обновляем dailyGoal расчетным значением
                dailyGoal = if (!profile.isManualGoal) {
                    recommendedResult.recommendedGoal
                } else {
                    _state.value.settings.dailyGoal
                }
            )

            // 3. Сохраняем
            updateUserSettingsUseCase(newSettings)
                .onSuccess {
                    _state.update {
                        it.copy(recommendedGoal = recommendedResult)
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Failed to update profile")
                    }
                    globalSnackbarController.showError(exception.message ?: "Failed to update profile")
                }
        }
    }

    private fun calculateRecommendedGoalForProfile(profile: UserProfile) {
        viewModelScope.launch {
            val result = calculateRecommendedGoalUseCase(profile)
            _state.update {
                it.copy(recommendedGoal = result)
            }
        }
    }

    private fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            updateDailyGoalUseCase(goal)
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Failed to update daily goal")
                    }
                    globalSnackbarController.showError(exception.message ?: "Failed to update daily goal")
                }
        }
    }

    private fun updateSettings(settings: UserSettings) {
        viewModelScope.launch {
            updateUserSettingsUseCase(settings)
                .onSuccess {
                    // Обновляем расписание уведомлений
                    notificationScheduler.scheduleNotifications(settings)
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Failed to update settings")
                    }
                    globalSnackbarController.showError(exception.message ?: "Failed to update settings")
                }
        }
    }

    private fun updateWakeUpTime(time: LocalTime) {
        viewModelScope.launch {
            val newSettings = _state.value.settings.copy(wakeUpTime = time)
            updateUserSettingsUseCase(newSettings)
                .onSuccess {
                    // Обновляем расписание уведомлений
                    notificationScheduler.scheduleNotifications(newSettings)
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Failed to update wake up time")
                    }
                    globalSnackbarController.showError(exception.message ?: "Failed to update wake up time")
                }
        }
    }

    private fun updateBedTime(time: LocalTime) {
        viewModelScope.launch {
            val newSettings = _state.value.settings.copy(bedTime = time)
            updateUserSettingsUseCase(newSettings)
                .onSuccess {
                    // Обновляем расписание уведомлений
                    notificationScheduler.scheduleNotifications(newSettings)
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Failed to update bed time")
                    }
                    globalSnackbarController.showError(exception.message ?: "Failed to update bed time")
                }
        }
    }

    private fun updateQuickAmounts(amounts: List<QuickAddPreset>) {
        viewModelScope.launch {
            val newSettings = _state.value.settings.copy(quickAddPresets = amounts)
            updateUserSettingsUseCase(newSettings)
                .onFailure { exception ->
                    _state.update {
                        it.copy(error = exception.message ?: "Failed to update quick amounts")
                    }
                    globalSnackbarController.showError(exception.message ?: "Failed to update quick amounts")
                }
        }
    }
}