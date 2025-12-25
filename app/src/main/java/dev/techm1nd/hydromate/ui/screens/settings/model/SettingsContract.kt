package dev.techm1nd.hydromate.ui.screens.settings.model

import dev.techm1nd.hydromate.domain.entities.*
import dev.techm1nd.hydromate.domain.usecases.hydration.RecommendedGoalResult
import java.time.LocalTime

data class SettingsState(
    val drinks: List<Drink> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val recommendedGoal: RecommendedGoalResult = RecommendedGoalResult(),
    val isLoading: Boolean = false,
    val error: String? = "",
    val showGoalDialog: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val showProfileDialog: Boolean = false,
    val timePickerType: TimePickerType = TimePickerType.WAKE_UP,
    val selectedCharacter: CharacterType = CharacterType.PENGUIN
)

sealed class SettingsIntent {
    data class UpdateDailyGoal(val goal: Int) : SettingsIntent()
    data class UpdateWakeUpTime(val time: LocalTime) : SettingsIntent()
    data class UpdateBedTime(val time: LocalTime) : SettingsIntent()
    data class UpdateQuickAmounts(val amounts: List<QuickAddPreset>) : SettingsIntent()

    data class UpdateProfile(val profile: UserProfile) : SettingsIntent()
    data class CalculateRecommendedGoal(val profile: UserProfile) : SettingsIntent()

    data class UpdateSettings(val settings: UserSettings) : SettingsIntent()

    object ShowGoalDialog : SettingsIntent()
    object HideGoalDialog : SettingsIntent()
    object ShowProfileDialog : SettingsIntent()
    object HideProfileDialog : SettingsIntent()
    data class ShowTimePickerDialog(val type: TimePickerType) : SettingsIntent()
    object HideTimePickerDialog : SettingsIntent()

    object RefreshSettings : SettingsIntent()
    object ClearError : SettingsIntent()
}

enum class TimePickerType {
    WAKE_UP, BED_TIME
}