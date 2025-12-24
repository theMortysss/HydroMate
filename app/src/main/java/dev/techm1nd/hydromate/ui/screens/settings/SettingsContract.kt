package dev.techm1nd.hydromate.ui.screens.settings

import dev.techm1nd.hydromate.domain.entities.*
import dev.techm1nd.hydromate.domain.usecases.hydration.RecommendedGoalResult
import java.time.LocalTime

data class SettingsUiState(
    val drinks: List<Drink> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val recommendedGoal: RecommendedGoalResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showGoalDialog: Boolean = false,
//    val showCharacterDialog: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val showProfileDialog: Boolean = false,
    val timePickerType: TimePickerType = TimePickerType.WAKE_UP,
    val selectedCharacter: CharacterType = CharacterType.PENGUIN
)

sealed class SettingsIntent {
    data class UpdateDailyGoal(val goal: Int) : SettingsIntent()
//    data class UpdateCharacter(val character: CharacterType) : SettingsIntent()
//    data class UpdateNotifications(val enabled: Boolean) : SettingsIntent()
//    data class UpdateNotificationInterval(val intervalMinutes: Int) : SettingsIntent()
    data class UpdateWakeUpTime(val time: LocalTime) : SettingsIntent()
    data class UpdateBedTime(val time: LocalTime) : SettingsIntent()
    data class UpdateQuickAmounts(val amounts: List<QuickAddPreset>) : SettingsIntent()
//    data class UpdateShowNetHydration(val show: Boolean) : SettingsIntent()

    data class UpdateProfile(val profile: UserProfile) : SettingsIntent()
    data class CalculateRecommendedGoal(val profile: UserProfile) : SettingsIntent()

    data class UpdateSettings(val settings: UserSettings) : SettingsIntent()

    object ShowGoalDialog : SettingsIntent()
    object HideGoalDialog : SettingsIntent()
//    object ShowCharacterDialog : SettingsIntent()
//    object HideCharacterDialog : SettingsIntent()
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