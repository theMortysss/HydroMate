package sdf.bitt.hydromate.ui.screens.home

import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.entities.DrinkType
import sdf.bitt.hydromate.domain.entities.UserSettings
import sdf.bitt.hydromate.domain.usecases.CalculateCharacterStateUseCase

data class HomeUiState(
    val todayProgress: DailyProgress? = null,
    val userSettings: UserSettings? = null,
    val characterState: CalculateCharacterStateUseCase.CharacterState = CalculateCharacterStateUseCase.CharacterState.CONTENT,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingWater: Boolean = false
) {
    val quickAmounts: List<Int>
        get() = userSettings?.quickAmounts ?: listOf(250, 500, 750)

    val progressPercentage: Float
        get() = todayProgress?.progressPercentage ?: 0f

    val currentAmount: Int
        get() = todayProgress?.totalAmount ?: 0

    val goalAmount: Int
        get() = todayProgress?.goalAmount ?: 2000

    val remainingAmount: Int
        get() = todayProgress?.remainingAmount ?: goalAmount
}

sealed class HomeIntent {
    data class AddWater(val amount: Int, val type: DrinkType = DrinkType.WATER) : HomeIntent()
    data class DeleteEntry(val entryId: Long) : HomeIntent()
    object RefreshData : HomeIntent()
    object ClearError : HomeIntent()
}

sealed class HomeEffect {
    object ShowAddWaterAnimation : HomeEffect()
    object ShowGoalReachedCelebration : HomeEffect()
    data class ShowError(val message: String) : HomeEffect()
    object HapticFeedback : HomeEffect()
}
