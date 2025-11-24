package sdf.bitt.hydromate.domain.usecases

import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.WaterEntry
import javax.inject.Inject

/**
 * Расчет эффективной гидратации с учетом типа напитка
 */
class CalculateHydrationUseCase @Inject constructor() {

    /**
     * Рассчитывает эффективную гидратацию для одной записи
     */
    operator fun invoke(amount: Int, drink: Drink): HydrationResult {
        val effectiveAmount = drink.calculateHydration(amount)
        val dehydrationAmount = if (drink.containsAlcohol || drink.containsCaffeine) {
            calculateDehydrationEffect(amount, drink)
        } else 0

        return HydrationResult(
            actualAmount = amount,
            effectiveAmount = effectiveAmount,
            dehydrationAmount = dehydrationAmount,
            netHydration = effectiveAmount - dehydrationAmount,
            drink = drink
        )
    }

    /**
     * Рассчитывает суммарную гидратацию для списка записей
     */
    fun calculateTotal(entries: List<WaterEntry>, drinks: Map<Long, Drink>): TotalHydration {
        var totalActual = 0
        var totalEffective = 0
        var totalDehydration = 0

        val drinkBreakdown = mutableMapOf<Drink, Int>()

        entries.forEach { entry ->
            val drink = drinks[entry.drinkId] ?: Drink.WATER
            val result = invoke(entry.amount, drink)

            totalActual += result.actualAmount
            totalEffective += result.effectiveAmount
            totalDehydration += result.dehydrationAmount

            drinkBreakdown[drink] = (drinkBreakdown[drink] ?: 0) + entry.amount
        }

        return TotalHydration(
            totalActual = totalActual,
            totalEffective = totalEffective,
            totalDehydration = totalDehydration,
            netHydration = totalEffective - totalDehydration,
            drinkBreakdown = drinkBreakdown
        )
    }

    /**
     * Расчет дегидратирующего эффекта кофеина/алкоголя
     */
    private fun calculateDehydrationEffect(amount: Int, drink: Drink): Int {
        return when {
            drink.containsAlcohol -> (amount * 0.15f).toInt() // Алкоголь ~15% дегидратация
            drink.containsCaffeine -> (amount * 0.05f).toInt() // Кофеин ~5% дегидратация
            else -> 0
        }
    }

    /**
     * Рассчитывает процент гидратации относительно цели
     */
    fun calculateProgress(
        netHydration: Int,
        dailyGoal: Int,
        hydrationThreshold: Float = 1.0f
    ): HydrationProgress {
        val adjustedGoal = (dailyGoal * hydrationThreshold).toInt()
        val percentage = (netHydration.toFloat() / adjustedGoal * 100).coerceIn(0f, 100f)
        val remaining = (adjustedGoal - netHydration).coerceAtLeast(0)

        return HydrationProgress(
            current = netHydration,
            goal = adjustedGoal,
            percentage = percentage,
            remaining = remaining,
            isGoalReached = netHydration >= adjustedGoal
        )
    }
}

/**
 * Результат расчета гидратации для одной записи
 */
data class HydrationResult(
    val actualAmount: Int, // Фактический объем напитка
    val effectiveAmount: Int, // Эффективная гидратация
    val dehydrationAmount: Int, // Дегидратирующий эффект
    val netHydration: Int, // Чистая гидратация
    val drink: Drink
)

/**
 * Суммарная гидратация за период
 */
data class TotalHydration(
    val totalActual: Int, // Общий объем выпитого
    val totalEffective: Int, // Общая эффективная гидратация
    val totalDehydration: Int, // Общая дегидратация
    val netHydration: Int, // Чистая гидратация
    val drinkBreakdown: Map<Drink, Int> // Разбивка по напиткам
)

/**
 * Прогресс гидратации относительно цели
 */
data class HydrationProgress(
    val current: Int,
    val goal: Int,
    val percentage: Float,
    val remaining: Int,
    val isGoalReached: Boolean
)