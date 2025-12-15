package sdf.bitt.hydromate.ui.screens.home

import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.QuickAddPreset
import sdf.bitt.hydromate.domain.entities.UserSettings
import sdf.bitt.hydromate.domain.usecases.CalculateCharacterStateUseCase
import sdf.bitt.hydromate.domain.usecases.HydrationProgress
import sdf.bitt.hydromate.domain.usecases.TotalHydration
import java.sql.Timestamp
import java.time.LocalDateTime

data class HomeUiState(
    val todayProgress: DailyProgress? = null,
    val userSettings: UserSettings? = null,
    val characterState: CalculateCharacterStateUseCase.CharacterState =
        CalculateCharacterStateUseCase.CharacterState.CONTENT,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingWater: Boolean = false,
    val totalHydration: TotalHydration? = null,
    val hydrationProgress: HydrationProgress? = null,
    val drinks: List<Drink> = emptyList(),
    val selectedDrink: Drink? = null,
    val selectedCharacter: CharacterType = CharacterType.PENGUIN
) {
    val progressPercentage: Float
        get() = hydrationProgress?.percentage?.div(100f) ?: 0f

    val currentAmount: Int
        get() = if (userSettings?.showNetHydration == true) {
            totalHydration?.netHydration ?: 0
        } else {
            todayProgress?.totalAmount ?: 0
        }

    val goalAmount: Int
        get() = hydrationProgress?.goal ?: todayProgress?.goalAmount ?: 2000

    val remainingAmount: Int
        get() = hydrationProgress?.remaining ?: 0
}

sealed class HomeIntent {
    data class AddWater(val amount: Int, val drink: Drink, val timestamp: LocalDateTime = LocalDateTime.now()) : HomeIntent()
    data class DeleteEntry(val entryId: Long) : HomeIntent()
    data class SelectDrink(val drink: Drink) : HomeIntent()
    data class CreateCustomDrink(val drink: Drink) : HomeIntent()

    // NEW: Управление Quick Add Presets
    data class UpdateQuickPresets(val presets: List<QuickAddPreset>) : HomeIntent()

    object RefreshData : HomeIntent()
    object ClearError : HomeIntent()
}

sealed class HomeEffect {
    object ShowAddWaterAnimation : HomeEffect()
    object ShowGoalReachedCelebration : HomeEffect()
    data class ShowError(val message: String) : HomeEffect()
    data class ShowSuccess(val message: String) : HomeEffect()
    object HapticFeedback : HomeEffect()

    data class ShowHydrationInfo(
        val actualAmount: Int,
        val effectiveAmount: Int,
        val netHydration: Int,
        val drink: Drink
    ) : HomeEffect()
}