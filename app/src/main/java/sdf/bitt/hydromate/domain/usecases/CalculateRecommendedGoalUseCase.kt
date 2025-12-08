package sdf.bitt.hydromate.domain.usecases

import sdf.bitt.hydromate.domain.entities.*
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Расчет рекомендуемой суточной нормы гидратации
 *
 * Основано на рекомендациях:
 * - Institute of Medicine (IOM)
 * - European Food Safety Authority (EFSA)
 * - World Health Organization (WHO)
 *
 * Базовая формула: вес (кг) × 30-35 мл
 * С учетом факторов: пол, активность, климат, особые состояния
 */
class CalculateRecommendedGoalUseCase @Inject constructor() {

    /**
     * Рассчитать рекомендуемую норму на основе профиля
     */
    operator fun invoke(profile: UserProfile): RecommendedGoalResult {
        if (!profile.isValid()) {
            return RecommendedGoalResult(
                recommendedGoal = 2000,
                breakdown = GoalBreakdown(
                    baseAmount = 2000,
                    genderAdjustment = 0,
                    activityAdjustment = 0,
                    climateAdjustment = 0,
                    totalRecommended = 2000
                ),
                explanation = "Invalid profile data. Using default 2000ml.",
                isDefault = true
            )
        }

        // 1. Базовый расчет по весу и полу
        val baseAmount = calculateBaseAmount(profile.weightKg, profile.gender)

        // 2. Корректировка по полу и особым состояниям
        val genderAdjustment = calculateGenderAdjustment(profile.gender)

        // 3. Корректировка по уровню активности
        val activityAdjustment = profile.activityLevel.additionalMl

        // 4. Корректировка по климату
        val climateAdjustment = profile.climate.additionalMl

        // 5. Итоговая сумма
        val totalRecommended =
            (baseAmount + genderAdjustment + activityAdjustment + climateAdjustment)

        val breakdown = GoalBreakdown(
            baseAmount = baseAmount,
            genderAdjustment = genderAdjustment,
            activityAdjustment = activityAdjustment,
            climateAdjustment = climateAdjustment,
            totalRecommended = totalRecommended
        )

        val explanation = buildExplanation(profile, breakdown)

        return RecommendedGoalResult(
            recommendedGoal = totalRecommended,
            breakdown = breakdown,
            explanation = explanation,
            isDefault = false
        )
    }

    /**
     * Базовый расчет: вес × базовый коэффициент
     *
     * Стандартные рекомендации:
     * - Взрослые: 32-37 мл/кг
     * - Для упрощения используем 34 мл/кг как средний показатель
     */
    private fun calculateBaseAmount(weightKg: Int, gender: Gender): Int {
        // Базовая формула: вес × 33мл
        // Но учитываем базовые различия по полу
        val baseCoefficient = when (gender) {
            Gender.MALE -> 37 // Мужчинам требуется немного больше
            Gender.FEMALE -> 32 // Женщинам немного меньше
            Gender.PREGNANT, Gender.BREASTFEEDING -> 32 // Базово как у женщин, + добавка отдельно
            Gender.PREFER_NOT_TO_SAY -> 34 // Средний показатель
        }

        var baseAmount = 30 * baseCoefficient
        for (i in 31..weightKg) {
            baseAmount += if (i > 50) { 20 } else { 10 }
        }
        return baseAmount
    }

    /**
     * Корректировка по полу и особым состояниям
     *
     * Рекомендации:
     * - Беременность: +200-400мл в зависимости от триместра
     * - Кормление грудью: +600-800мл (производство молока требует жидкости)
     */
    private fun calculateGenderAdjustment(gender: Gender): Int {
        return when (gender) {
            Gender.PREGNANT -> 300 // +300мл для беременных (средний показатель)
            Gender.BREASTFEEDING -> 700 // +700мл для кормящих (WHO рекомендация)
            else -> 0
        }
    }

    /**
     * Построение понятного объяснения расчета
     */
    private fun buildExplanation(profile: UserProfile, breakdown: GoalBreakdown): String {
        val parts = mutableListOf<String>()

        // Базовая часть
        parts.add("Based on your weight (${profile.weightKg}kg): ${breakdown.baseAmount}ml")

        // Пол
        if (breakdown.genderAdjustment != 0) {
            val sign = if (breakdown.genderAdjustment > 0) "+" else ""
            parts.add("${profile.gender.displayName}: $sign${breakdown.genderAdjustment}ml")
        }

        // Активность
        if (breakdown.activityAdjustment != 0) {
            parts.add("${profile.activityLevel.displayName}: +${breakdown.activityAdjustment}ml")
        }

        // Климат
        if (breakdown.climateAdjustment != 0) {
            parts.add("${profile.climate.displayName} climate: +${breakdown.climateAdjustment}ml")
        }

        return parts.joinToString("\n")
    }

    /**
     * Получить диапазон здоровой нормы (±20% от рекомендуемой)
     */
    fun getHealthyRange(recommendedGoal: Int): IntRange {
        val lower = (recommendedGoal * 0.8).roundToInt()
        val upper = (recommendedGoal * 1.2).roundToInt()
        return lower..upper
    }
}

/**
 * Результат расчета рекомендуемой нормы
 */
data class RecommendedGoalResult(
    val recommendedGoal: Int,
    val breakdown: GoalBreakdown,
    val explanation: String,
    val isDefault: Boolean = false
) {
    /**
     * Минимальная рекомендуемая норма (80% от основной)
     */
    val minimumRecommended: Int
        get() = (recommendedGoal * 0.8).roundToInt()

    /**
     * Оптимальная верхняя граница (120% от основной)
     */
    val maximumRecommended: Int
        get() = (recommendedGoal * 1.2).roundToInt()
}

/**
 * Детальная разбивка расчета нормы
 */
data class GoalBreakdown(
    val baseAmount: Int,
    val genderAdjustment: Int,
    val activityAdjustment: Int,
    val climateAdjustment: Int,
    val totalRecommended: Int
)