package sdf.bitt.hydromate.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.QuickAddPreset
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import sdf.bitt.hydromate.domain.usecases.UpdateDailyGoalUseCase
import sdf.bitt.hydromate.domain.usecases.UpdateUserSettingsUseCase
import sdf.bitt.hydromate.ui.notification.NotificationScheduler
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    private val updateDailyGoalUseCase: UpdateDailyGoalUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateDailyGoal -> updateDailyGoal(intent.goal)
            is SettingsIntent.UpdateCharacter -> updateCharacter(intent.character)
            is SettingsIntent.UpdateNotifications -> updateNotifications(intent.enabled)
            is SettingsIntent.UpdateNotificationInterval -> updateNotificationInterval(intent.intervalMinutes)
            is SettingsIntent.UpdateWakeUpTime -> updateWakeUpTime(intent.time)
            is SettingsIntent.UpdateBedTime -> updateBedTime(intent.time)
            is SettingsIntent.UpdateQuickAmounts -> updateQuickAmounts(intent.amounts)

            SettingsIntent.ShowGoalDialog -> _uiState.update { it.copy(showGoalDialog = true) }
            SettingsIntent.HideGoalDialog -> _uiState.update { it.copy(showGoalDialog = false) }
            SettingsIntent.ShowCharacterDialog -> _uiState.update { it.copy(showCharacterDialog = true) }
            SettingsIntent.HideCharacterDialog -> _uiState.update { it.copy(showCharacterDialog = false) }
            is SettingsIntent.ShowTimePickerDialog -> _uiState.update {
                it.copy(showTimePickerDialog = true, timePickerType = intent.type)
            }
            SettingsIntent.HideTimePickerDialog -> _uiState.update { it.copy(showTimePickerDialog = false) }

            SettingsIntent.RefreshSettings -> observeSettings()
            SettingsIntent.ClearError -> _uiState.update { it.copy(error = null) }

            // NEW: Обработка настроек гидратации
            is SettingsIntent.UpdateHydrationThreshold -> updateHydrationThreshold(intent.threshold)
            is SettingsIntent.UpdateShowNetHydration -> updateShowNetHydration(intent.show)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getUserSettingsUseCase()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load settings"
                        )
                    }
                }
                .collect { settings ->
                    _uiState.update {
                        it.copy(
                            settings = settings,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            updateDailyGoalUseCase(goal)
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update daily goal")
                    }
                }
        }
    }

    private fun updateCharacter(character: CharacterType) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(selectedCharacter = character)
            updateUserSettingsUseCase(newSettings)
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update character")
                    }
                }
        }
    }

    private fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(notificationsEnabled = enabled)
            updateUserSettingsUseCase(newSettings)
                .onSuccess {
                    // Обновляем расписание уведомлений
                    notificationScheduler.scheduleNotifications(newSettings)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update notifications")
                    }
                }
        }
    }

    private fun updateNotificationInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(notificationInterval = intervalMinutes)
            updateUserSettingsUseCase(newSettings)
                .onSuccess {
                    // Обновляем расписание уведомлений
                    notificationScheduler.scheduleNotifications(newSettings)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update notification interval")
                    }
                }
        }
    }

    private fun updateWakeUpTime(time: LocalTime) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(wakeUpTime = time)
            updateUserSettingsUseCase(newSettings)
                .onSuccess {
                    // Обновляем расписание уведомлений
                    notificationScheduler.scheduleNotifications(newSettings)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update wake up time")
                    }
                }
        }
    }

    private fun updateBedTime(time: LocalTime) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(bedTime = time)
            updateUserSettingsUseCase(newSettings)
                .onSuccess {
                    // Обновляем расписание уведомлений
                    notificationScheduler.scheduleNotifications(newSettings)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update bed time")
                    }
                }
        }
    }

    private fun updateQuickAmounts(amounts: List<QuickAddPreset>) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(quickAddPresets = amounts)
            updateUserSettingsUseCase(newSettings)
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update quick amounts")
                    }
                }
        }
    }

    // NEW: Методы для настроек гидратации
    private fun updateHydrationThreshold(threshold: Float) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(
                hydrationThreshold = threshold.coerceIn(0.8f, 1.2f)
            )
            updateUserSettingsUseCase(newSettings)
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update hydration threshold")
                    }
                }
        }
    }

    private fun updateShowNetHydration(show: Boolean) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(showNetHydration = show)
            updateUserSettingsUseCase(newSettings)
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update display mode")
                    }
                }
        }
    }
}