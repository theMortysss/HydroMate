package sdf.bitt.hydromate.ui.screens.home

import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.DrinkType
import sdf.bitt.hydromate.domain.entities.UserSettings
import sdf.bitt.hydromate.domain.usecases.CalculateCharacterStateUseCase
import sdf.bitt.hydromate.domain.usecases.HydrationProgress
import sdf.bitt.hydromate.domain.usecases.TotalHydration

data class HomeUiState(
    val todayProgress: DailyProgress? = null,
    val userSettings: UserSettings? = null,
    val characterState: CalculateCharacterStateUseCase.CharacterState = CalculateCharacterStateUseCase.CharacterState.CONTENT,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingWater: Boolean = false,

    // NEW: Данные гидратации
    val totalHydration: TotalHydration? = null,
    val hydrationProgress: HydrationProgress? = null,
    val drinks: List<Drink> = emptyList(),
    val selectedDrink: Drink? = null
) {
    val quickAmounts: List<Int>
        get() = userSettings?.quickAmounts ?: listOf(250, 500, 750)

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
    // Обновлено: теперь принимает Drink вместо DrinkType
    data class AddWater(val amount: Int, val drink: Drink) : HomeIntent()
    data class DeleteEntry(val entryId: Long) : HomeIntent()

    // NEW: Управление напитками
    data class SelectDrink(val drink: Drink) : HomeIntent()
    data class CreateCustomDrink(val drink: Drink) : HomeIntent()

    object RefreshData : HomeIntent()
    object ClearError : HomeIntent()
}

sealed class HomeEffect {
    object ShowAddWaterAnimation : HomeEffect()
    object ShowGoalReachedCelebration : HomeEffect()
    data class ShowError(val message: String) : HomeEffect()
    data class ShowSuccess(val message: String) : HomeEffect() // NEW
    object HapticFeedback : HomeEffect()

    // NEW: Эффекты для гидратации
    data class ShowHydrationInfo(
        val actualAmount: Int,
        val effectiveAmount: Int,
        val netHydration: Int,
        val drink: Drink
    ) : HomeEffect()
}