package sdf.bitt.hydromate.domain.entities

/**
 * Профиль пользователя для расчета рекомендуемой нормы гидратации
 */
data class UserProfile(
    val gender: Gender = Gender.PREFER_NOT_TO_SAY,
    val weightKg: Int = 70,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val climate: Climate = Climate.MODERATE,
    val isManualGoal: Boolean = false, // Если true, игнорируем расчет и используем ручную норму
    val manualGoal: Int = 2000 // Ручная норма (если isManualGoal = true)
) {
    /**
     * Валидация профиля
     */
    fun isValid(): Boolean {
        return weightKg in 30..200 && manualGoal in 500..5000
    }

    /**
     * Получить текущую цель (ручная или расчетная будет определена в UseCase)
     */
    fun getCurrentGoal(calculatedGoal: Int): Int {
        return if (isManualGoal) manualGoal else calculatedGoal
    }
}

/**
 * Пол пользователя (учитывается при расчете нормы)
 */
enum class Gender(val displayName: String, val icon: String) {
    MALE("Male", "👨"),
    FEMALE("Female", "👩"),
    PREGNANT("Pregnant", "🤰"),
    BREASTFEEDING("Breastfeeding", "🤱"),
    PREFER_NOT_TO_SAY("Prefer not to say", "👤");

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
        displayName = "Low Activity",
        icon = "🛋️",
        additionalMl = 0
    ),
    MODERATE(
        displayName = "Moderate Activity",
        icon = "🚶",
        additionalMl = 300
    ),
    HIGH(
        displayName = "High Activity",
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
        displayName = "Cold",
        icon = "❄️",
        description = "Cold climate, minimal sweating",
        additionalMl = 0
    ),
    MODERATE(
        displayName = "Moderate",
        icon = "🌤️",
        description = "Comfortable temperature",
        additionalMl = 200
    ),
    WARM(
        displayName = "Warm",
        icon = "☀️",
        description = "Warm weather, increased sweating",
        additionalMl = 450
    ),
    HOT(
        displayName = "Hot",
        icon = "🔥",
        description = "Hot climate, high perspiration",
        additionalMl = 700
    );

    companion object {
        fun fromString(value: String): Climate {
            return values().find { it.name == value } ?: MODERATE
        }
    }
}