package dev.techm1nd.hydromate.domain.entities

import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Тип челленджа (что запрещено/разрешено)
 */
enum class ChallengeType(
    val displayName: String,
    val description: String,
    val icon: String,
    val difficultyLevel: ChallengeDifficulty
) {
    NO_CAFFEINE(
        displayName = "Без кофеина",
        description = "Никаких напитков с кофеином",
        icon = "☕",
        difficultyLevel = ChallengeDifficulty.MEDIUM
    ),
    NO_ALCOHOL(
        displayName = "Без алкоголя",
        description = "Никаких алкогольных напитков",
        icon = "🍺",
        difficultyLevel = ChallengeDifficulty.HARD
    ),
    WATER_ONLY(
        displayName = "Только вода",
        description = "Пить только воду",
        icon = "💧",
        difficultyLevel = ChallengeDifficulty.HARD
    ),
    NO_LACTOSE(
        displayName = "Без лактозы",
        description = "Без молочных продуктов",
        icon = "🥛",
        difficultyLevel = ChallengeDifficulty.EASY
    ),
    NO_SUGAR(
        displayName = "Без сахара",
        description = "Без напитков с добавленным сахаром",
        icon = "🍬",
        difficultyLevel = ChallengeDifficulty.MEDIUM
    ),
    NO_SODA(
        displayName = "Без газировки",
        description = "Без газированных напитков",
        icon = "🥤",
        difficultyLevel = ChallengeDifficulty.EASY
    ),
    PLANT_BASED(
        displayName = "Растительные напитки",
        description = "Только растительные напитки",
        icon = "🌱",
        difficultyLevel = ChallengeDifficulty.MEDIUM
    ),
    HYDRATION_HERO(
        displayName = "Герой гидратации",
        description = "Достигай дневной цели каждый день",
        icon = "🏆",
        difficultyLevel = ChallengeDifficulty.MEDIUM
    );

    /**
     * Проверяет, нарушает ли напиток челлендж
     */
    fun isViolated(drink: Drink): Boolean {
        return when (this) {
            NO_CAFFEINE -> drink.containsCaffeine
            NO_ALCOHOL -> drink.containsAlcohol
            WATER_ONLY -> drink.category != DrinkType.WATER
            NO_LACTOSE -> drink.category == DrinkType.DAIRY && !drink.isCustom
            NO_SUGAR -> drink.category == DrinkType.SOFT_DRINKS ||
                    drink.name.contains("Сироп", ignoreCase = true)
            NO_SODA -> drink.category == DrinkType.SOFT_DRINKS ||
                    drink.category == DrinkType.BRANDS
            PLANT_BASED -> drink.category == DrinkType.DAIRY &&
                    !drink.name.contains("Миндаль", ignoreCase = true) &&
                    !drink.name.contains("Соя", ignoreCase = true) &&
                    !drink.name.contains("Овёс", ignoreCase = true)
            HYDRATION_HERO -> false // Проверяется отдельно
        }
    }
}

/**
 * Сложность челленджа
 */
enum class ChallengeDifficulty(
    val displayName: String,
    val xpReward: Int,
    val color: String
) {
    EASY("Легко", 300, "#4CAF50"),
    MEDIUM("Средне", 400, "#FF9800"),
    HARD("Сложно", 600, "#F44336")
}

/**
 * Челлендж
 */
data class Challenge(
    val id: String,
    val type: ChallengeType,
    val durationDays: Int = 14,
    val startDate: LocalDate,
    val endDate: LocalDate = startDate.plusDays(durationDays.toLong() - 1),
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val currentStreak: Int = 0,
    val violations: List<ChallengeViolation> = emptyList()
) {
    /**
     * Прогресс в процентах
     */
    val progressPercentage: Float
        get() {
            val today = LocalDate.now()
            return when {
                today.isBefore(startDate) -> 0f
                today.isAfter(endDate) -> 100f
                else -> {
                    val totalDays = durationDays.toFloat()
                    val daysPassed = java.time.temporal.ChronoUnit.DAYS
                        .between(startDate, today).toFloat() + 1
                    ((daysPassed / totalDays) * 100).coerceIn(0f, 100f)
                }
            }
        }

    /**
     * Дней осталось
     */
    val daysRemaining: Int
        get() {
            val today = LocalDate.now()
            return if (today.isAfter(endDate)) 0
            else java.time.temporal.ChronoUnit.DAYS.between(today, endDate).toInt() + 1
        }

    /**
     * Дней пройдено
     */
    val daysPassed: Int
        get() = durationDays - daysRemaining

    /**
     * Награда за прохождение
     */
    val xpReward: Int
        get() = type.difficultyLevel.xpReward

    companion object {
        /**
         * Создать новый челлендж
         */
        fun create(
            type: ChallengeType,
            startDate: LocalDate = LocalDate.now()
        ): Challenge {
            return Challenge(
                id = java.util.UUID.randomUUID().toString(),
                type = type,
                startDate = startDate
            )
        }
    }
}

/**
 * Нарушение челленджа
 */
@Serializable
data class ChallengeViolation(
    val date: String,
    val drinkName: String,
    val drinkIcon: String
) {
    companion object {
        fun create(date: LocalDate, drinkName: String, drinkIcon: String): ChallengeViolation {
            return ChallengeViolation(
                date = date.toString(),
                drinkName = drinkName,
                drinkIcon = drinkIcon
            )
        }
    }

    fun getDate(): LocalDate {
        return LocalDate.parse(date)
    }
}
