package dev.techm1nd.hydromate.domain.entities

/**
 * Профиль пользователя для расчета рекомендуемой нормы гидратации
 */
data class UserProfile(
    // Базовая информация
    val gender: Gender = Gender.PREFER_NOT_TO_SAY,
    val weightKg: Int = 70,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val climate: Climate = Climate.MODERATE,
    val isManualGoal: Boolean = false,
    val manualGoal: Int = 2000,

    // Прогрессия
    val level: Int = 1,
    val currentXP: Int = 0,
    val totalXP: Int = 0,
    val selectedCharacter: CharacterType = CharacterType.PENGUIN,
    val unlockedCharacters: Set<CharacterType> = setOf(CharacterType.PENGUIN),

    // Статистика
    val totalDrinksDrank: Int = 0,
    val uniqueDrinksTried: Set<String> = emptySet(),
    val challengesCompleted: Int = 0,
    val achievementsUnlocked: Int = 0
) {
    /**
     * XP необходимый для следующего уровня
     */
    val xpForNextLevel: Int
        get() = level * 200

    /**
     * Прогресс до следующего уровня (0–100%)
     */
    val levelProgress: Float
        get() = (currentXP.toFloat() / xpForNextLevel * 100).coerceIn(0f, 100f)

    /**
     * Добавить XP
     */
    fun addXP(xp: Int): UserProfile {
        val newCurrentXP = currentXP + xp
        val newTotalXP = totalXP + xp

        // Проверка на повышение уровня
        return if (newCurrentXP >= xpForNextLevel) {
            val remainingXP = newCurrentXP - xpForNextLevel
            this.copy(
                level = level + 1,
                currentXP = remainingXP,
                totalXP = newTotalXP
            )
        } else {
            this.copy(
                currentXP = newCurrentXP,
                totalXP = newTotalXP
            )
        }
    }

    /**
     * Разблокировать персонажа
     */
    fun unlockCharacter(character: CharacterType): UserProfile {
        return this.copy(
            unlockedCharacters = unlockedCharacters + character
        )
    }

    /**
     * Проверка, разблокирован ли персонаж
     */
    fun isCharacterUnlocked(character: CharacterType): Boolean {
        return character.isUnlockedByDefault || unlockedCharacters.contains(character)
    }

    fun isValid(): Boolean {
        return weightKg in 30..200 && manualGoal in 500..5000
    }

    fun getCurrentGoal(calculatedGoal: Int): Int {
        return if (isManualGoal) manualGoal else calculatedGoal
    }
}

/**
 * Пол пользователя (учитывается при расчете нормы)
 */
enum class Gender(val displayName: String, val icon: String) {
    MALE("Мужской", "👨"),
    FEMALE("Женский", "👩"),
    PREGNANT("Беременность", "🤰"),
    BREASTFEEDING("Грудное вскармливание", "🤱"),
    PREFER_NOT_TO_SAY("Предпочитаю не указывать", "👤");

    companion object {
        /**
         * Получить из строки (для совместимости с базой данных)
         */
        fun fromString(value: String): Gender {
            return values().find { it.name == value } ?: PREFER_NOT_TO_SAY
        }
    }
}

/**
 * Уровень физической активности
 */
enum class ActivityLevel(
    val displayName: String,
    val icon: String,
    val additionalMl: Int
) {
    LOW(
        displayName = "Низкая активность",
        icon = "🛋️",
        additionalMl = 0
    ),
    MODERATE(
        displayName = "Умеренная активность",
        icon = "🚶",
        additionalMl = 300
    ),
    HIGH(
        displayName = "Высокая активность",
        icon = "🏃",
        additionalMl = 700
    );

    companion object {
        fun fromString(value: String): ActivityLevel {
            return values().find { it.name == value } ?: MODERATE
        }
    }
}

/**
 * Климатические условия
 */
enum class Climate(
    val displayName: String,
    val icon: String,
    val description: String,
    val additionalMl: Int
) {
    COLD(
        displayName = "Холодный",
        icon = "❄️",
        description = "Холодный климат, минимальное потоотделение",
        additionalMl = 0
    ),
    MODERATE(
        displayName = "Умеренный",
        icon = "🌤️",
        description = "Комфортная температура",
        additionalMl = 200
    ),
    WARM(
        displayName = "Тёплый",
        icon = "☀️",
        description = "Тёплая погода, повышенное потоотделение",
        additionalMl = 450
    ),
    HOT(
        displayName = "Жаркий",
        icon = "🔥",
        description = "Жаркий климат, сильное потоотделение",
        additionalMl = 700
    );

    companion object {
        fun fromString(value: String): Climate {
            return values().find { it.name == value } ?: MODERATE
        }
    }
}