package dev.techm1nd.hydromate.domain.usecases.hydration

import kotlinx.coroutines.flow.first
import dev.techm1nd.hydromate.domain.repositories.DrinkRepository
import dev.techm1nd.hydromate.domain.usecases.stat.GetTodayProgressUseCase
import javax.inject.Inject

/**
 * Проверяет, достигнута ли дневная цель по гидратации
 */
class CheckGoalReachedUseCase @Inject constructor(
    private val getTodayProgressUseCase: GetTodayProgressUseCase,
    private val drinkRepository: DrinkRepository,
    private val calculateHydrationUseCase: CalculateHydrationUseCase
) {

    /**
     * Проверяет достижение цели с учетом всех настроек
     * @return true если цель достигнута, false иначе
     */
    suspend operator fun invoke(): Result<Boolean> {
        return try {
            val progress = getTodayProgressUseCase().first()

            // Получаем напитки для расчета гидратации
            val drinks = drinkRepository.getAllActiveDrinks().first()
            val drinksMap = drinks.associateBy { it.id }

            // Рассчитываем гидратацию
            val hydration = if (progress.entries.isNotEmpty()) {
                calculateHydrationUseCase.calculateTotal(progress.entries, drinksMap)
            } else {
                null
            }

            // Определяем текущее количество
//            val currentAmount = if (settings.showNetHydration) {
//                hydration?.netHydration ?: 0
//            } else {
//                progress.totalAmount
//            }

            val currentAmount = hydration?.netHydration ?: 0

            // Учитываем порог гидратации
            val adjustedGoal = progress.goalAmount

            val isGoalReached = currentAmount >= adjustedGoal

            Result.success(isGoalReached)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Возвращает детальную информацию о прогрессе
     */
    suspend fun getDetailedProgress(): Result<GoalProgress> {
        return try {
            val progress = getTodayProgressUseCase().first()

            val drinks = drinkRepository.getAllActiveDrinks().first()
            val drinksMap = drinks.associateBy { it.id }

            val hydration = if (progress.entries.isNotEmpty()) {
                calculateHydrationUseCase.calculateTotal(progress.entries, drinksMap)
            } else {
                null
            }

//            val currentAmount = if (settings.showNetHydration) {
//                hydration?.netHydration ?: 0
//            } else {
//                progress.totalAmount
//            }

            val currentAmount = hydration?.netHydration ?: 0

            val adjustedGoal = progress.goalAmount
            val percentage = ((currentAmount.toFloat() / adjustedGoal) * 100).toInt()
            val remaining = (adjustedGoal - currentAmount).coerceAtLeast(0)

            Result.success(
                GoalProgress(
                    currentAmount = currentAmount,
                    goalAmount = adjustedGoal,
                    percentage = percentage,
                    remaining = remaining,
                    isGoalReached = currentAmount >= adjustedGoal
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Детальная информация о прогрессе достижения цели
 */
data class GoalProgress(
    val currentAmount: Int,
    val goalAmount: Int,
    val percentage: Int,
    val remaining: Int,
    val isGoalReached: Boolean
)