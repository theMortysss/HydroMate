package dev.techm1nd.hydromate.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.techm1nd.hydromate.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.entities.UserProfile
import dev.techm1nd.hydromate.domain.usecases.hydration.CalculateRecommendedGoalUseCase
import dev.techm1nd.hydromate.domain.usecases.setting.GetUserSettingsUseCase
import dev.techm1nd.hydromate.domain.usecases.setting.UpdateUserSettingsUseCase
import dev.techm1nd.hydromate.ui.screens.onboarding.model.*
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarController
import dev.techm1nd.hydromate.utils.UiText
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    private val calculateRecommendedGoalUseCase: CalculateRecommendedGoalUseCase,
    private val globalSnackbarController: GlobalSnackbarController
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects: Flow<OnboardingEffect> = _effects.receiveAsFlow()

    init {
        loadCurrentSettings()
    }

    fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.UpdateProfile -> updateProfile(intent.profile)
            is OnboardingIntent.SelectGoal -> selectGoal(intent.goal, intent.isManual)
            OnboardingIntent.NextStep -> nextStep()
            OnboardingIntent.PreviousStep -> previousStep()
            OnboardingIntent.CompleteOnboarding -> completeOnboarding()
            OnboardingIntent.SkipOnboarding -> skipOnboarding()
        }
    }

    private fun loadCurrentSettings() {
        viewModelScope.launch {
            getUserSettingsUseCase().first().let { settings ->
                _state.update {
                    it.copy(
                        profile = settings.profile,
                        recommendedGoal = if (!settings.profile.isManualGoal) {
                            calculateRecommendedGoalUseCase(settings.profile)
                        } else {
                            it.recommendedGoal
                        }
                    )
                }
            }
        }
    }

    private fun updateProfile(profile: UserProfile) {
        _state.update { it.copy(profile = profile) }

        // Пересчитываем рекомендацию при изменении профиля
        viewModelScope.launch {
            val recommendation = calculateRecommendedGoalUseCase(profile)
            _state.update { it.copy(recommendedGoal = recommendation) }
        }
    }

    private fun selectGoal(goal: Int, isManual: Boolean) {
        _state.update {
            it.copy(
                profile = it.profile.copy(
                    isManualGoal = isManual,
                    manualGoal = goal
                )
            )
        }
    }

    private fun nextStep() {
        val currentStep = _state.value.currentStep
        val nextStep = when (currentStep) {
            OnboardingStep.WELCOME -> OnboardingStep.PROFILE
            OnboardingStep.PROFILE -> {
                // Валидация профиля
                if (!_state.value.profile.isValid()) {
                    globalSnackbarController.showError(UiText.StringResource(R.string.onboarding_profile_validation_error))
                    return
                }
                OnboardingStep.GOAL
            }

            OnboardingStep.GOAL -> OnboardingStep.COMPLETE
            OnboardingStep.COMPLETE -> {
                completeOnboarding()
                return
            }
        }

        _state.update { it.copy(currentStep = nextStep) }
    }

    private fun previousStep() {
        val currentStep = _state.value.currentStep
        val previousStep = when (currentStep) {
            OnboardingStep.WELCOME -> return
            OnboardingStep.PROFILE -> OnboardingStep.WELCOME
            OnboardingStep.GOAL -> OnboardingStep.PROFILE
            OnboardingStep.COMPLETE -> OnboardingStep.GOAL
        }

        _state.update { it.copy(currentStep = previousStep) }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val currentSettings = getUserSettingsUseCase().first()
            val profile = _state.value.profile

            // Определяем финальную цель
            val finalGoal = if (profile.isManualGoal) {
                profile.manualGoal
            } else {
                _state.value.recommendedGoal.recommendedGoal
            }

            val updatedSettings = currentSettings.copy(
                profile = profile,
                dailyGoal = finalGoal,
                onboardingCompleted = true
            )

            updateUserSettingsUseCase(updatedSettings)
                .onSuccess {
                    globalSnackbarController.showSuccess(UiText.StringResource(R.string.onboarding_settings_saved))
                    _effects.trySend(OnboardingEffect.NavigateToHome)
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to save settings"
                        )
                    }
                    globalSnackbarController.showError(
                        UiText.DynamicString(exception.message ?: "Failed to save settings")
                    )
                }
        }
    }

    private fun skipOnboarding() {
        viewModelScope.launch {
            // Используем дефолтные настройки (2000ml)
            val currentSettings = getUserSettingsUseCase().first()
            val defaultSettings = currentSettings.copy(
                dailyGoal = 2000,
            )

            updateUserSettingsUseCase(defaultSettings)
                .onSuccess {
                    _effects.trySend(OnboardingEffect.NavigateToHome)
                }
                .onFailure { exception ->
                    globalSnackbarController.showError(
                        UiText.DynamicString(exception.message ?: "Failed to save settings")
                    )
                }
        }
    }
}