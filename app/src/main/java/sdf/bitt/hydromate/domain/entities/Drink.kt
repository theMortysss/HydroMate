package sdf.bitt.hydromate.domain.entities

/**
 * Представляет напиток с параметрами гидратации
 */
data class Drink(
    val id: Long = 0,
    val name: String,
    val icon: String, // Emoji или icon resource
    val hydrationMultiplier: Float, // Коэффициент гидратации (0.0 - 1.2)
    val category: DrinkType,
    val containsCaffeine: Boolean = false,
    val containsAlcohol: Boolean = false,
    val isCustom: Boolean = false,
    val color: String = "#2196F3" // Hex color for UI
) {
    /**
     * Рассчитывает эффективное количество воды с учетом гидратации
     * @param amount количество напитка в мл
     * @return эффективное количество воды в мл
     */
    fun calculateHydration(amount: Int): Int {
        return (amount * hydrationMultiplier).toInt()
    }

    companion object {
        // Предустановленные напитки
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
            hydrationMultiplier = 0.95f,
            category = DrinkType.HOT_BEVERAGES,
            containsCaffeine = true,
            color = "#a77242"
        )

        val HERBAL_TEA = Drink(
            id = 3,
            name = "Herbal Tea",
            icon = "🫖",
            hydrationMultiplier = 1.0f,
            category = DrinkType.HOT_BEVERAGES,
            color = "#55afd6"
        )

        val COFFEE = Drink(
            id = 4,
            name = "Coffee",
            icon = "☕",
            hydrationMultiplier = 0.85f,
            category = DrinkType.HOT_BEVERAGES,
            containsCaffeine = true,
            color = "#95663b"
        )

        val JUICE = Drink(
            id = 5,
            name = "Juice",
            icon = "🧃",
            hydrationMultiplier = 0.9f,
            category = DrinkType.JUICES,
            color = "#376ab7"
        )

        val SMOOTHIE = Drink(
            id = 6,
            name = "Smoothie",
            icon = "🥤",
            hydrationMultiplier = 0.85f,
            category = DrinkType.JUICES,
            color = "#3669b5"
        )

        val MILK = Drink(
            id = 7,
            name = "Milk",
            icon = "🥛",
            hydrationMultiplier = 0.9f,
            category = DrinkType.DAIRY,
            color = "#376ab7"
        )

        val COCONUT_WATER = Drink(
            id = 8,
            name = "Coconut Water",
            icon = "🥥",
            hydrationMultiplier = 1.1f,
            category = DrinkType.SPORTS,
            color = "#57b3db"
        )

        val SPORTS_DRINK = Drink(
            id = 9,
            name = "Sports Drink",
            icon = "⚡",
            hydrationMultiplier = 1.0f,
            category = DrinkType.SPORTS,
            color = "#55afd6"
        )

        val SODA = Drink(
            id = 10,
            name = "Soda",
            icon = "🥤",
            hydrationMultiplier = 0.7f,
            category = DrinkType.SOFT_DRINKS,
            color = "#c68727"
        )

        val ENERGY_DRINK = Drink(
            id = 11,
            name = "Energy Drink",
            icon = "⚡",
            hydrationMultiplier = 0.75f,
            category = DrinkType.SOFT_DRINKS,
            containsCaffeine = true,
            color = "#835a34"
        )

        val BEER = Drink(
            id = 12,
            name = "Beer",
            icon = "🍺",
            hydrationMultiplier = 0.5f,
            category = DrinkType.ALCOHOL,
            containsAlcohol = true,
            color = "#a33030"
        )

        val WINE = Drink(
            id = 13,
            name = "Wine",
            icon = "🍷",
            hydrationMultiplier = 0.4f,
            category = DrinkType.ALCOHOL,
            containsAlcohol = true,
            color = "#841a1a"
        )

        val COCKTAIL = Drink(
            id = 14,
            name = "Cocktail",
            icon = "🍹",
            hydrationMultiplier = 0.6f,
            category = DrinkType.ALCOHOL,
            containsAlcohol = true,
            color = "#a83232"
        )

        val SOUP = Drink(
            id = 15,
            name = "Soup",
            icon = "🍲",
            hydrationMultiplier = 0.8f,
            category = DrinkType.FOOD,
            color = "#3567b2"
        )

        fun getDefaultDrinks(): List<Drink> = listOf(
            WATER, TEA, HERBAL_TEA, COFFEE, JUICE, SMOOTHIE,
            MILK, COCONUT_WATER, SPORTS_DRINK, SODA,
            ENERGY_DRINK, BEER, WINE, COCKTAIL, SOUP
        )
    }
}

/**
 * Категории напитков для группировки
 */
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