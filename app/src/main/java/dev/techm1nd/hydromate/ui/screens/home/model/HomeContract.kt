package dev.techm1nd.hydromate.ui.screens.home.model

import dev.techm1nd.hydromate.domain.entities.CharacterType
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.entities.QuickAddPreset
import dev.techm1nd.hydromate.domain.entities.UserSettings
import dev.techm1nd.hydromate.domain.usecases.character.CalculateCharacterStateUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.HydrationProgress
import dev.techm1nd.hydromate.domain.usecases.hydration.TotalHydration
import java.time.LocalDateTime

data class HomeState(
    val todayProgress: DailyProgress = DailyProgress(),
    val userSettings: UserSettings = UserSettings(),
    val characterState: CalculateCharacterStateUseCase.CharacterState =
        CalculateCharacterStateUseCase.CharacterState.CONTENT,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingWater: Boolean = false,
    val totalHydration: TotalHydration = TotalHydration(),
    val hydrationProgress: HydrationProgress = HydrationProgress(),
    val drinks: List<Drink> = emptyList(),
    val selectedDrink: Drink? = null,
    val selectedCharacter: CharacterType = CharacterType.PENGUIN,
    val viewedTipIds: Set<String> = emptySet(),
) {
    val progressPercentage: Float
        get() = hydrationProgress.percentage.div(100f)

    val currentAmount: Int
        get() = totalHydration.netHydration

    val goalAmount: Int
        get() =
            if (hydrationProgress.goal != 0) hydrationProgress.goal
            else if (todayProgress.goalAmount != 0) todayProgress.goalAmount
            else 2000

    val remainingAmount: Int
        get() = hydrationProgress.remaining
}

sealed class HomeIntent {
    data class AddWater(
        val amount: Int,
        val drink: Drink,
        val timestamp: LocalDateTime = LocalDateTime.now()
    ) : HomeIntent()

    data class DeleteEntry(val entryId: Long) : HomeIntent()
    data class SelectDrink(val drink: Drink) : HomeIntent()
    data class CreateCustomDrink(val drink: Drink) : HomeIntent()
    data class UpdateQuickPresets(val presets: List<QuickAddPreset>) : HomeIntent()
    data class MarkTipAsViewed(val tipId: String) : HomeIntent()

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