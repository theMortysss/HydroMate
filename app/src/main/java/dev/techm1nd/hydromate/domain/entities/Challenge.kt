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
        displayName = "Caffeine-Free",
        description = "No drinks with caffeine",
        icon = "☕",
        difficultyLevel = ChallengeDifficulty.MEDIUM
    ),
    NO_ALCOHOL(
        displayName = "Alcohol-Free",
        description = "No alcoholic drinks",
        icon = "🍺",
        difficultyLevel = ChallengeDifficulty.HARD
    ),
    WATER_ONLY(
        displayName = "Water Only",
        description = "Drink only water",
        icon = "💧",
        difficultyLevel = ChallengeDifficulty.HARD
    ),
    NO_LACTOSE(
        displayName = "Lactose-Free",
        description = "No dairy products",
        icon = "🥛",
        difficultyLevel = ChallengeDifficulty.EASY
    ),
    NO_SUGAR(
        displayName = "Sugar-Free",
        description = "No drinks with added sugar",
        icon = "🍬",
        difficultyLevel = ChallengeDifficulty.MEDIUM
    ),
    NO_SODA(
        displayName = "Soda-Free",
        description = "No carbonated soft drinks",
        icon = "🥤",
        difficultyLevel = ChallengeDifficulty.EASY
    ),
    PLANT_BASED(
        displayName = "Plant-Based",
        description = "Only plant-based drinks",
        icon = "🌱",
        difficultyLevel = ChallengeDifficulty.MEDIUM
    ),
    HYDRATION_HERO(
        displayName = "Hydration Hero",
        description = "Reach daily goal every day",
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
                    drink.name.contains("Syrup", ignoreCase = true)
            NO_SODA -> drink.category == DrinkType.SOFT_DRINKS ||
                    drink.category == DrinkType.BRANDS
            PLANT_BASED -> drink.category == DrinkType.DAIRY &&
                    !drink.name.contains("Almond", ignoreCase = true) &&
                    !drink.name.contains("Soy", ignoreCase = true) &&
                    !drink.name.contains("Oat", ignoreCase = true)
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
    EASY("Easy", 300, "#4CAF50"),
    MEDIUM("Medium", 400, "#FF9800"),
    HARD("Hard", 600, "#F44336")
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
    val date: String, // Changed from LocalDate to String for serialization
    val drinkName: String,
    val drinkIcon: String
) {
    companion object {
        fun create(date: LocalDate, drinkName: String, drinkIcon: String): ChallengeViolation {
            return ChallengeViolation(
                date = date.toString(), // Convert LocalDate to ISO-8601 string
                drinkName = drinkName,
                drinkIcon = drinkIcon
            )
        }
    }

    fun getDate(): LocalDate {
        return LocalDate.parse(date) // Parse ISO-8601 string back to LocalDate
    }
}
