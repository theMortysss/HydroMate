package dev.techm1nd.hydromate.domain.usecases.hydration

import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.entities.WaterEntry
import javax.inject.Inject
import kotlin.math.max

/**
 * Расчет эффективной гидратации на основе научных исследований
 *
 * Источники:
 * - Maughan et al. (2016) - Beverage Hydration Index
 * - Killer et al. (2014) - Caffeine effects
 * - Hobson & Maughan (2010) - Alcohol dehydration
 */
class CalculateHydrationUseCase @Inject constructor() {

    /**
     * Рассчитывает эффективную гидратацию для одной записи
     */
    operator fun invoke(amount: Int, drink: Drink): HydrationResult {
        // 1. Базовая гидратация с учетом типа напитка
        val baseHydration = (amount * drink.hydrationMultiplier).toInt()

//        // 2. Расчет эффекта кофеина (основано на исследованиях)
//        val caffeineEffect = calculateCaffeineEffect(amount, drink.caffeineContent)
//
//        // 3. Расчет эффекта алкоголя (основано на исследованиях)
//        val alcoholEffect = calculateAlcoholEffect(amount, drink.alcoholPercentage)

//        // 4. Общий дегидратирующий эффект
//        val totalDehydration = caffeineEffect + alcoholEffect

//        // 5. Чистая гидратация
//        val netHydration = max(0, baseHydration - totalDehydration)
        val netHydration = max(0, baseHydration)

        return HydrationResult(
            actualAmount = amount,
            effectiveAmount = baseHydration,
            netHydration = netHydration,
            drink = drink
        )
    }

    /**
     * Расчет эффекта кофеина на основе исследований
     *
     * Исследования показывают:
     * - Умеренное потребление кофеина (до 400мг/день) имеет минимальный диуретический эффект
     * - При регулярном потреблении развивается толерантность
     * - Эффект зависит от дозы кофеина
     *
     * Формула: dehydration = caffeine_mg * volume_factor * dose_multiplier
     */
    private fun calculateCaffeineEffect(volumeMl: Int, caffeineContentPer250ml: Int): Int {
        if (caffeineContentPer250ml == 0) return 0

        // Рассчитываем общее количество кофеина
        val totalCaffeine = (volumeMl / 250f) * caffeineContentPer250ml

        // Коэффициенты основаны на исследованиях:
        // - До 100мг: минимальный эффект (~2%)
        // - 100-200мг: легкий эффект (~3-4%)
        // - 200-300мг: умеренный эффект (~5-6%)
        // - 300мг+: выраженный эффект (~7-10%)
        val effectMultiplier = when {
            totalCaffeine < 100 -> 0.02f
            totalCaffeine < 200 -> 0.035f
            totalCaffeine < 300 -> 0.055f
            else -> {
                // Прогрессивное увеличение эффекта
                val excessCaffeine = totalCaffeine - 300
                0.07f + (excessCaffeine / 1000f)
            }
        }

        return (volumeMl * effectMultiplier).toInt()
    }

    /**
     * Расчет эффекта алкоголя на основе исследований
     *
     * Исследования показывают:
     * - Алкоголь подавляет вазопрессин (ADH), увеличивая диурез
     * - Эффект пропорционален концентрации алкоголя
     * - Крепкие напитки имеют более выраженный эффект
     *
     * Основная формула из исследований:
     * Диурез увеличивается примерно на 10мл на каждый грамм алкоголя
     *
     * 1 стандартный напиток = 14г алкоголя = ~140мл дополнительного диуреза
     */
    private fun calculateAlcoholEffect(volumeMl: Int, alcoholPercentage: Float): Int {
        if (alcoholPercentage <= 0f) return 0

        // Рассчитываем граммы алкоголя
        // Плотность этанола = 0.789 г/мл
        val alcoholVolume = volumeMl * (alcoholPercentage / 100f)
        val alcoholGrams = alcoholVolume * 0.789f

        // Базовый диуретический эффект: ~10мл на грамм алкоголя
        val baseDiuresis = alcoholGrams * 10f

        // Дополнительные факторы:
        // 1. Концентрация (более крепкие напитки имеют более сильный эффект)
        val concentrationFactor = when {
            alcoholPercentage < 5f -> 1.0f  // Пиво
            alcoholPercentage < 15f -> 1.2f // Вино
            alcoholPercentage < 25f -> 1.4f // Крепленые вина
            else -> 1.6f                    // Крепкие напитки
        }

        // 2. Объемный фактор (большие объемы усиливают эффект)
        val volumeFactor = when {
            volumeMl < 150 -> 0.8f
            volumeMl < 350 -> 1.0f
            else -> 1.2f
        }

        val totalDiuresis = baseDiuresis * concentrationFactor * volumeFactor

        // Чистый дегидратирующий эффект = диурез - объем жидкости
        // (часть жидкости компенсирует потери)
        val netDehydration = totalDiuresis - (volumeMl * 0.3f)

        return max(0, netDehydration.toInt())
    }

    /**
     * Рассчитывает суммарную гидратацию для списка записей
     */
    fun calculateTotal(entries: List<WaterEntry>, drinks: Map<Long, Drink>): TotalHydration {
        var totalActual = 0
        var totalEffective = 0
//        var totalCaffeineDehydration = 0
//        var totalAlcoholDehydration = 0
//        var totalDehydration = 0

        val drinkBreakdown = mutableMapOf<Drink, Int>()

        entries.forEach { entry ->
            val drink = drinks[entry.drinkId] ?: Drink.WATER
            val result = invoke(entry.amount, drink)

            totalActual += result.actualAmount
            totalEffective += result.effectiveAmount
//            totalCaffeineDehydration += result.caffeineDehydration
//            totalAlcoholDehydration += result.alcoholDehydration
//            totalDehydration += result.totalDehydration

            drinkBreakdown[drink] = (drinkBreakdown[drink] ?: 0) + entry.amount
        }

        return TotalHydration(
            totalActual = totalActual,
            totalEffective = totalEffective,
//            caffeineDehydration = totalCaffeineDehydration,
//            alcoholDehydration = totalAlcoholDehydration,
//            totalDehydration = totalDehydration,
//            netHydration = totalEffective - totalDehydration,
            netHydration = totalEffective,
            drinkBreakdown = drinkBreakdown
        )
    }

    /**
     * Рассчитывает процент гидратации относительно цели
     */
    fun calculateProgress(
        netHydration: Int,
        dailyGoal: Int,
    ): HydrationProgress {
        val percentage = (netHydration.toFloat() / dailyGoal * 100).coerceIn(0f, 100f)
        val remaining = (dailyGoal - netHydration).coerceAtLeast(0)

        return HydrationProgress(
            current = netHydration,
            goal = dailyGoal,
            percentage = percentage,
            remaining = remaining,
            isGoalReached = netHydration >= dailyGoal
        )
    }
}

/**
 * Результат расчета гидратации для одной записи
 */
data class HydrationResult(
    val actualAmount: Int,
    val effectiveAmount: Int,
    val netHydration: Int,
    val drink: Drink
)

/**
 * Суммарная гидратация за период
 */
data class TotalHydration(
    val totalActual: Int,
    val totalEffective: Int,
    val netHydration: Int,
    val drinkBreakdown: Map<Drink, Int>
)

data class HydrationProgress(
    val current: Int,
    val goal: Int,
    val percentage: Float,
    val remaining: Int,
    val isGoalReached: Boolean
)
