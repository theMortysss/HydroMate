package sdf.bitt.hydromate.domain.entities

/**
 * Представляет напиток с параметрами гидратации
 */
data class Drink(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val hydrationMultiplier: Float, // Базовый коэффициент гидратации (0.0 - 1.2)
    val category: DrinkType,
    val caffeineContent: Int = 0, // мг на 250мл
    val alcoholPercentage: Float = 0f, // % алкоголя
    val isCustom: Boolean = false,
    val color: String = "#2196F3"
) {
    val containsCaffeine: Boolean
        get() = caffeineContent > 0

    val containsAlcohol: Boolean
        get() = alcoholPercentage > 0f

    val alcoholCategory: AlcoholCategory
        get() = AlcoholCategory.fromPercentage(alcoholPercentage)

    // TODO добавить больше напитков
    companion object {
        // Предустановленные напитки с научно обоснованными значениями
        val WATER = Drink(
            id = 1,
            name = "Water",
            icon = "💧",
            hydrationMultiplier = 1.0f,
            category = DrinkType.WATER,
            color = "#55afd6"
        )

        val TEA = Drink(
            id = 2,
            name = "Tea",
            icon = "🍵",
            hydrationMultiplier = 0.9f,
            category = DrinkType.HOT_BEVERAGES,
            caffeineContent = 25, // ~25mg в 250мл
            color = "#a77242"
        )

        val HERBAL_TEA = Drink(
            id = 3,
            name = "Herbal Tea",
            icon = "🫖",
            hydrationMultiplier = 0.9f,
            category = DrinkType.HOT_BEVERAGES,
            color = "#55afd6"
        )

        val COFFEE = Drink(
            id = 4,
            name = "Coffee",
            icon = "☕",
            hydrationMultiplier = 0.6f,
            category = DrinkType.HOT_BEVERAGES,
            caffeineContent = 95, // ~95mg в 250мл
            color = "#95663b"
        )

        val ESPRESSO = Drink(
            id = 5,
            name = "Espresso",
            icon = "☕",
            hydrationMultiplier = 0.4f,
            category = DrinkType.HOT_BEVERAGES,
            caffeineContent = 500, // ~500mg в порции
            color = "#6b4423"
        )

        val JUICE = Drink(
            id = 6,
            name = "Juice",
            icon = "🧃",
            hydrationMultiplier = 0.95f,
            category = DrinkType.JUICES,
            color = "#376ab7"
        )

        val SMOOTHIE = Drink(
            id = 7,
            name = "Smoothie",
            icon = "🥤",
            hydrationMultiplier = 0.7f,
            category = DrinkType.JUICES,
            color = "#3669b5"
        )

        val MILK = Drink(
            id = 8,
            name = "Milk",
            icon = "🥛",
            hydrationMultiplier = 1.3f,
            category = DrinkType.DAIRY,
            color = "#376ab7"
        )

        val COCONUT_WATER = Drink(
            id = 9,
            name = "Coconut Water",
            icon = "🥥",
            hydrationMultiplier = 0.9f,
            category = DrinkType.SPORTS,
            color = "#57b3db"
        )

        val SPORTS_DRINK = Drink(
            id = 10,
            name = "Sports Drink",
            icon = "⚡",
            hydrationMultiplier = 0.96f,
            category = DrinkType.SPORTS,
            color = "#55afd6"
        )

        val SODA = Drink(
            id = 11,
            name = "Soda",
            icon = "🥤",
            hydrationMultiplier = 0.8f,
            category = DrinkType.SOFT_DRINKS,
            color = "#c68727"
        )

        val ENERGY_DRINK = Drink(
            id = 12,
            name = "Energy Drink",
            icon = "⚡",
            hydrationMultiplier = 0.55f,
            category = DrinkType.SOFT_DRINKS,
            caffeineContent = 80, // ~80mg в 250мл
            color = "#835a34"
        )

        val BEER_LIGHT = Drink(
            id = 13,
            name = "Light Beer",
            icon = "🍺",
            hydrationMultiplier = -0.4f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 3.5f,
            color = "#d4a574"
        )

        val BEER_REGULAR = Drink(
            id = 14,
            name = "Regular Beer",
            icon = "🍺",
            hydrationMultiplier = -0.70f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 6.0f,
            color = "#c89048"
        )

        val WINE = Drink(
            id = 15,
            name = "Wine",
            icon = "🍷",
            hydrationMultiplier = -0.95f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 12.0f,
            color = "#841a1a"
        )
        // TODO добавить популярные коктейли
        val COCKTAIL = Drink(
            id = 16,
            name = "Cocktail",
            icon = "🍹",
            hydrationMultiplier = -0.5f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 15.0f,
            color = "#a83232"
        )

        val SPIRITS = Drink(
            id = 17,
            name = "Spirits",
            icon = "🥃",
            hydrationMultiplier = -3.18f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 40.0f,
            color = "#5c1a1a"
        )

        val SOUP = Drink(
            id = 18,
            name = "Soup",
            icon = "🍲",
            hydrationMultiplier = 0.6f,
            category = DrinkType.FOOD,
            color = "#3567b2"
        )

        fun getDefaultDrinks(): List<Drink> = listOf(
            WATER, HERBAL_TEA, TEA, COFFEE, ESPRESSO,
            JUICE, SMOOTHIE, MILK, COCONUT_WATER, SPORTS_DRINK,
            SODA, ENERGY_DRINK, BEER_LIGHT, BEER_REGULAR,
            WINE, COCKTAIL, SPIRITS, SOUP
        )
    }
}

/**
 * Категории алкоголя по крепости
 */
enum class AlcoholCategory(val displayName: String, val minPercent: Float, val maxPercent: Float) {
    NONE("No Alcohol", 0f, 0f),
    VERY_LIGHT("Very Light (1-4%)", 1f, 4f),
    LIGHT("Light (4-5%)", 4f, 5f),
    MODERATE("Moderate (5-8%)", 5f, 8f),
    MEDIUM("Medium (8-15%)", 8f, 15f),
    STRONG("Strong (15%+)", 15f, 100f);

    companion object {
        fun fromPercentage(percentage: Float): AlcoholCategory {
            return when {
                percentage <= 0f -> NONE
                percentage < 4f -> VERY_LIGHT
                percentage < 5f -> LIGHT
                percentage < 8f -> MODERATE
                percentage < 15f -> MEDIUM
                else -> STRONG
            }
        }
    }
}

/**
 * Уровни кофеина
 */
enum class CaffeineLevel(val displayName: String, val minMg: Int, val maxMg: Int) {
    NONE("No Caffeine", 0, 0),
    LOW("Low (1-40mg)", 1, 40),
    MODERATE("Moderate (40-80mg)", 40, 80),
    HIGH("High (80-150mg)", 80, 150),
    VERY_HIGH("Very High (150mg+)", 150, 500);

    companion object {
        fun fromMg(mg: Int): CaffeineLevel {
            return when {
                mg <= 0 -> NONE
                mg < 40 -> LOW
                mg < 80 -> MODERATE
                mg < 150 -> HIGH
                else -> VERY_HIGH
            }
        }
    }
}

enum class DrinkType(val displayName: String, val icon: String) {
    WATER("Water", "💧"),
    HOT_BEVERAGES("Hot Beverages", "☕"),
    JUICES("Juices & Smoothies", "🧃"),
    DAIRY("Dairy", "🥛"),
    SPORTS("Sports Drinks", "⚡"),
    SOFT_DRINKS("Soft Drinks", "🥤"),
    ALCOHOL("Alcohol", "🍺"),
    FOOD("Food & Soup", "🍲"),
    CUSTOM("Custom", "✨")
}