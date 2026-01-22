package dev.techm1nd.hydromate.ui.screens.onboarding.model

import dev.techm1nd.hydromate.domain.entities.UserProfile
import dev.techm1nd.hydromate.domain.usecases.hydration.RecommendedGoalResult

data class OnboardingState(
    val profile: UserProfile = UserProfile(),
    val recommendedGoal: RecommendedGoalResult = RecommendedGoalResult(),
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class OnboardingStep {
    WELCOME,      // Приветствие
    PROFILE,      // Настройка профиля (вес, пол, активность, климат)
    GOAL,         // Выбор цели (рекомендованная или ручная)
    COMPLETE      // Завершение
}

sealed class OnboardingIntent {
    data class UpdateProfile(val profile: UserProfile) : OnboardingIntent()
    data class SelectGoal(val goal: Int, val isManual: Boolean) : OnboardingIntent()
    object NextStep : OnboardingIntent()
    object PreviousStep : OnboardingIntent()
    object CompleteOnboarding : OnboardingIntent()
    object SkipOnboarding : OnboardingIntent()
}

sealed class OnboardingEffect {
    object NavigateToHome : OnboardingEffect()
    data class ShowError(val message: String) : OnboardingEffect()
}