package sdf.bitt.hydromate.ui.screens.settings

import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.UserSettings
import java.time.LocalTime

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showGoalDialog: Boolean = false,
    val showCharacterDialog: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val timePickerType: TimePickerType = TimePickerType.WAKE_UP
)

sealed class SettingsIntent {
    data class UpdateDailyGoal(val goal: Int) : SettingsIntent()
    data class UpdateCharacter(val character: CharacterType) : SettingsIntent()
    data class UpdateNotifications(val enabled: Boolean) : SettingsIntent()
    data class UpdateNotificationInterval(val intervalMinutes: Int) : SettingsIntent()
    data class UpdateWakeUpTime(val time: LocalTime) : SettingsIntent()
    data class UpdateBedTime(val time: LocalTime) : SettingsIntent()
    data class UpdateQuickAmounts(val amounts: List<Int>) : SettingsIntent()

    object ShowGoalDialog : SettingsIntent()
    object HideGoalDialog : SettingsIntent()
    object ShowCharacterDialog : SettingsIntent()
    object HideCharacterDialog : SettingsIntent()
    data class ShowTimePickerDialog(val type: TimePickerType) : SettingsIntent()
    object HideTimePickerDialog : SettingsIntent()

    object RefreshSettings : SettingsIntent()
    object ClearError : SettingsIntent()
}

enum class TimePickerType {
    WAKE_UP, BED_TIME
}