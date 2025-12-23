package dev.techm1nd.hydromate.domain.entities

/**
 * Представляет напиток с параметрами гидратации
 */
data class Drink(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val hydrationMultiplier: Float,
    val category: DrinkType,
    val caffeineContent: Int = 0,
    val alcoholPercentage: Float = 0f,
    val isCustom: Boolean = false,
    val color: String = "#2196F3"
) {
    val containsCaffeine: Boolean
        get() = caffeineContent > 0

    val containsAlcohol: Boolean
        get() = alcoholPercentage > 0f

    val alcoholCategory: AlcoholCategory
        get() = AlcoholCategory.fromPercentage(alcoholPercentage)

    companion object {
        // === ВОДА ===
        val WATER = Drink(
            id = 1,
            name = "Water",
            icon = "💧",
            hydrationMultiplier = 1.0f,
            category = DrinkType.WATER,
            color = "#55afd6"
        )

        val MINERAL_WATER = Drink(
            id = 2,
            name = "Mineral Water",
            icon = "💎",
            hydrationMultiplier = 1.0f,
            category = DrinkType.WATER,
            color = "#4da6cc"
        )

        // === ЧАЙ (TEA) ===
        val HERBAL_TEA = Drink(
            id = 3,
            name = "Herbal Tea",
            icon = "🫖",
            hydrationMultiplier = 0.9f,
            category = DrinkType.TEA,
            color = "#8fbc8f"
        )

        val GREEN_TEA = Drink(
            id = 4,
            name = "Green Tea",
            icon = "🍵",
            hydrationMultiplier = 0.9f,
            category = DrinkType.TEA,
            caffeineContent = 30,
            color = "#90c695"
        )

        val BLACK_TEA = Drink(
            id = 5,
            name = "Black Tea",
            icon = "☕",
            hydrationMultiplier = 0.9f,
            category = DrinkType.TEA,
            caffeineContent = 47,
            color = "#8b4513"
        )

        val WHITE_TEA = Drink(
            id = 6,
            name = "White Tea",
            icon = "🫖",
            hydrationMultiplier = 0.95f,
            category = DrinkType.TEA,
            caffeineContent = 15,
            color = "#f5deb3"
        )

        val OOLONG_TEA = Drink(
            id = 7,
            name = "Oolong Tea",
            icon = "🍵",
            hydrationMultiplier = 0.9f,
            category = DrinkType.TEA,
            caffeineContent = 37,
            color = "#d2691e"
        )

        val FRUIT_TEA = Drink(
            id = 8,
            name = "Fruit Tea",
            icon = "🍊",
            hydrationMultiplier = 0.9f,
            category = DrinkType.TEA,
            color = "#ff6b6b"
        )

        val DECAF_TEA = Drink(
            id = 9,
            name = "Decaf Tea",
            icon = "🫖",
            hydrationMultiplier = 0.95f,
            category = DrinkType.TEA,
            color = "#a0c4a0"
        )

        val CHAI_LATTE = Drink(
            id = 10,
            name = "Chai Latte",
            icon = "☕",
            hydrationMultiplier = 1.0f,
            category = DrinkType.TEA,
            caffeineContent = 50,
            color = "#cd853f"
        )

        val ROYAL_MILK_TEA = Drink(
            id = 11,
            name = "Royal Milk Tea",
            icon = "🫖",
            hydrationMultiplier = 0.85f,
            category = DrinkType.TEA,
            caffeineContent = 40,
            color = "#d2b48c"
        )

        val CHRYSANTHEMUM_TEA = Drink(
            id = 12,
            name = "Chrysanthemum Tea",
            icon = "🌼",
            hydrationMultiplier = 0.95f,
            category = DrinkType.TEA,
            color = "#ffd700"
        )

        val BARLEY_TEA = Drink(
            id = 13,
            name = "Barley Tea",
            icon = "🌾",
            hydrationMultiplier = 0.9f,
            category = DrinkType.TEA,
            color = "#daa520"
        )

        // === КОФЕ (COFFEE) ===
        val COFFEE = Drink(
            id = 14,
            name = "Coffee",
            icon = "☕",
            hydrationMultiplier = 0.6f,
            category = DrinkType.COFFEE,
            caffeineContent = 95,
            color = "#6f4e37"
        )

        val ESPRESSO = Drink(
            id = 15,
            name = "Espresso",
            icon = "☕",
            hydrationMultiplier = 0.4f,
            category = DrinkType.COFFEE,
            caffeineContent = 500,
            color = "#3e2723"
        )

        val AMERICANO = Drink(
            id = 16,
            name = "Americano",
            icon = "☕",
            hydrationMultiplier = 0.7f,
            category = DrinkType.COFFEE,
            caffeineContent = 77,
            color = "#795548"
        )

        val LATTE = Drink(
            id = 17,
            name = "Latte",
            icon = "☕",
            hydrationMultiplier = 0.6f,
            category = DrinkType.COFFEE,
            caffeineContent = 75,
            color = "#bcaaa4"
        )

        val CAPPUCCINO = Drink(
            id = 18,
            name = "Cappuccino",
            icon = "☕",
            hydrationMultiplier = 0.6f,
            category = DrinkType.COFFEE,
            caffeineContent = 75,
            color = "#a1887f"
        )

        val MOCHA = Drink(
            id = 19,
            name = "Mocha",
            icon = "☕",
            hydrationMultiplier = 0.55f,
            category = DrinkType.COFFEE,
            caffeineContent = 95,
            color = "#8d6e63"
        )

        val MACCHIATO = Drink(
            id = 20,
            name = "Macchiato",
            icon = "☕",
            hydrationMultiplier = 0.45f,
            category = DrinkType.COFFEE,
            caffeineContent = 85,
            color = "#6d4c41"
        )

        val FLAT_WHITE = Drink(
            id = 21,
            name = "Flat White",
            icon = "☕",
            hydrationMultiplier = 0.7f,
            category = DrinkType.COFFEE,
            caffeineContent = 80,
            color = "#8d6e63"
        )

        val DECAF_COFFEE = Drink(
            id = 22,
            name = "Decaf Coffee",
            icon = "☕",
            hydrationMultiplier = 0.9f,
            category = DrinkType.COFFEE,
            caffeineContent = 5,
            color = "#9e7b6f"
        )

        val CHICORY = Drink(
            id = 23,
            name = "Chicory",
            icon = "☕",
            hydrationMultiplier = 0.85f,
            category = DrinkType.COFFEE,
            color = "#a0826d"
        )

        // === МОЛОЧНЫЕ (DAIRY) ===
        val MILK = Drink(
            id = 24,
            name = "Milk",
            icon = "🥛",
            hydrationMultiplier = 1.3f,
            category = DrinkType.DAIRY,
            color = "#f5f5f5"
        )

        val SKIM_MILK = Drink(
            id = 25,
            name = "Skim Milk",
            icon = "🥛",
            hydrationMultiplier = 0.91f,
            category = DrinkType.DAIRY,
            color = "#e8e8e8"
        )

        val ALMOND_MILK = Drink(
            id = 26,
            name = "Almond Milk",
            icon = "🥛",
            hydrationMultiplier = 0.9f,
            category = DrinkType.DAIRY,
            color = "#f0e5d8"
        )

        val SOY_MILK = Drink(
            id = 27,
            name = "Soy Milk",
            icon = "🥛",
            hydrationMultiplier = 0.9f,
            category = DrinkType.DAIRY,
            color = "#f5f5dc"
        )

        val OAT_MILK = Drink(
            id = 28,
            name = "Oat Milk",
            icon = "🥛",
            hydrationMultiplier = 0.89f,
            category = DrinkType.DAIRY,
            color = "#f4e4c1"
        )

        val KEFIR = Drink(
            id = 29,
            name = "Kefir",
            icon = "🥛",
            hydrationMultiplier = 0.7f,
            category = DrinkType.DAIRY,
            color = "#fffacd"
        )

        val YOGURT = Drink(
            id = 30,
            name = "Yogurt Drink",
            icon = "🥛",
            hydrationMultiplier = 0.7f,
            category = DrinkType.DAIRY,
            color = "#fff8dc"
        )

        val MILKSHAKE = Drink(
            id = 31,
            name = "Milkshake",
            icon = "🥤",
            hydrationMultiplier = 0.8f,
            category = DrinkType.DAIRY,
            color = "#ffc0cb"
        )

        // === ФРУКТОВЫЕ (JUICES) ===
        val JUICE = Drink(
            id = 32,
            name = "Juice",
            icon = "🧃",
            hydrationMultiplier = 0.95f,
            category = DrinkType.JUICES,
            color = "#ff8c00"
        )

        val ORANGE_JUICE = Drink(
            id = 33,
            name = "Orange Juice",
            icon = "🍊",
            hydrationMultiplier = -0.3f,
            category = DrinkType.JUICES,
            color = "#ffa500"
        )

        val COCONUT_WATER = Drink(
            id = 34,
            name = "Coconut Water",
            icon = "🥥",
            hydrationMultiplier = 0.9f,
            category = DrinkType.JUICES,
            color = "#f0fff0"
        )

        val LEMONADE = Drink(
            id = 35,
            name = "Lemonade",
            icon = "🍋",
            hydrationMultiplier = 0.9f,
            category = DrinkType.JUICES,
            color = "#fff44f"
        )

        val APPLE_SPRITZ = Drink(
            id = 36,
            name = "Apple Spritz",
            icon = "🍎",
            hydrationMultiplier = 0.9f,
            category = DrinkType.JUICES,
            color = "#8db600"
        )

        val SMOOTHIE = Drink(
            id = 37,
            name = "Smoothie",
            icon = "🥤",
            hydrationMultiplier = 0.6f,
            category = DrinkType.JUICES,
            color = "#ff69b4"
        )

        val ALOE_VERA_DRINK = Drink(
            id = 38,
            name = "Aloe Vera Drink",
            icon = "🌿",
            hydrationMultiplier = 0.85f,
            category = DrinkType.JUICES,
            color = "#90ee90"
        )

        // === АКТИВНЫЕ НАПИТКИ (SPORTS) ===
        val SPORTS_DRINK = Drink(
            id = 39,
            name = "Sports Drink",
            icon = "⚡",
            hydrationMultiplier = 0.96f,
            category = DrinkType.SPORTS,
            color = "#00bfff"
        )

        val ENERGY_DRINK = Drink(
            id = 40,
            name = "Energy Drink",
            icon = "⚡",
            hydrationMultiplier = 0.55f,
            category = DrinkType.SPORTS,
            caffeineContent = 80,
            color = "#ff4500"
        )

        val PROTEIN_SHAKE = Drink(
            id = 41,
            name = "Protein Shake",
            icon = "💪",
            hydrationMultiplier = 0.8f,
            category = DrinkType.SPORTS,
            color = "#dda0dd"
        )

        // === ГАЗИРОВКА (SOFT_DRINKS) ===
        val SODA = Drink(
            id = 42,
            name = "Soda",
            icon = "🥤",
            hydrationMultiplier = 0.83f,
            category = DrinkType.SOFT_DRINKS,
            color = "#c08030"
        )

        val DIET_SODA = Drink(
            id = 43,
            name = "Diet Soda",
            icon = "🥤",
            hydrationMultiplier = 0.83f,
            category = DrinkType.SOFT_DRINKS,
            color = "#b07030"
        )

        val GINGER_ALE = Drink(
            id = 44,
            name = "Ginger Ale",
            icon = "🥤",
            hydrationMultiplier = 0.85f,
            category = DrinkType.SOFT_DRINKS,
            color = "#f0e68c"
        )

        val ROOT_BEER = Drink(
            id = 45,
            name = "Root Beer",
            icon = "🥤",
            hydrationMultiplier = 0.8f,
            category = DrinkType.SOFT_DRINKS,
            color = "#8b4513"
        )

        val APEROL = Drink(
            id = 46,
            name = "Aperol",
            icon = "🍹",
            hydrationMultiplier = 0.8f,
            category = DrinkType.SOFT_DRINKS,
            color = "#ff6347"
        )

        // === МОКТЕЙЛИ (MOCKTAILS) ===
        val ARNOLD_PALMER = Drink(
            id = 47,
            name = "Arnold Palmer",
            icon = "🍹",
            hydrationMultiplier = 0.9f,
            category = DrinkType.MOCKTAILS,
            color = "#daa520"
        )

        val SHIRLEY_TEMPLE = Drink(
            id = 48,
            name = "Shirley Temple",
            icon = "🍹",
            hydrationMultiplier = 0.8f,
            category = DrinkType.MOCKTAILS,
            color = "#ff69b4"
        )

        val VIRGIN_MOJITO = Drink(
            id = 49,
            name = "Virgin Mojito",
            icon = "🍹",
            hydrationMultiplier = 0.9f,
            category = DrinkType.MOCKTAILS,
            color = "#98ff98"
        )

        val VIRGIN_BLOODY_MARY = Drink(
            id = 50,
            name = "Virgin Bloody Mary",
            icon = "🍹",
            hydrationMultiplier = 0.9f,
            category = DrinkType.MOCKTAILS,
            color = "#dc143c"
        )

        val VIRGIN_MARGARITA = Drink(
            id = 51,
            name = "Virgin Margarita",
            icon = "🍹",
            hydrationMultiplier = 0.85f,
            category = DrinkType.MOCKTAILS,
            color = "#00ff7f"
        )

        val VIRGIN_PINA_COLADA = Drink(
            id = 52,
            name = "Virgin Piña Colada",
            icon = "🍹",
            hydrationMultiplier = 0.85f,
            category = DrinkType.MOCKTAILS,
            color = "#fffacd"
        )

        val VIRGIN_COSMOPOLITAN = Drink(
            id = 53,
            name = "Virgin Cosmopolitan",
            icon = "🍹",
            hydrationMultiplier = 0.8f,
            category = DrinkType.MOCKTAILS,
            color = "#ff1493"
        )

        val VIRGIN_OLD_FASHIONED = Drink(
            id = 54,
            name = "Virgin Old Fashioned",
            icon = "🍹",
            hydrationMultiplier = -0.3f,
            category = DrinkType.MOCKTAILS,
            color = "#d2691e"
        )

        // === АЛКОГОЛЬ (ALCOHOL) ===
        val BEER_LIGHT = Drink(
            id = 55,
            name = "Light Beer",
            icon = "🍺",
            hydrationMultiplier = -0.4f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 3.5f,
            color = "#ffd700"
        )

        val BEER_REGULAR = Drink(
            id = 56,
            name = "Regular Beer",
            icon = "🍺",
            hydrationMultiplier = -0.70f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 6.0f,
            color = "#daa520"
        )

        val RED_WINE = Drink(
            id = 57,
            name = "Red Wine",
            icon = "🍷",
            hydrationMultiplier = -0.95f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 12.0f,
            color = "#8b0000"
        )

        val WHITE_WINE = Drink(
            id = 58,
            name = "White Wine",
            icon = "🍷",
            hydrationMultiplier = -0.95f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 12.0f,
            color = "#f0e68c"
        )

        val CHAMPAGNE = Drink(
            id = 59,
            name = "Champagne",
            icon = "🍾",
            hydrationMultiplier = -0.95f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 12.0f,
            color = "#f5deb3"
        )

        val VODKA = Drink(
            id = 60,
            name = "Vodka",
            icon = "🥃",
            hydrationMultiplier = -3.18f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 40.0f,
            color = "#e0e0e0"
        )

        val WHISKEY = Drink(
            id = 61,
            name = "Whiskey",
            icon = "🥃",
            hydrationMultiplier = -3.18f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 40.0f,
            color = "#d2691e"
        )

        val GIN = Drink(
            id = 62,
            name = "Gin",
            icon = "🥃",
            hydrationMultiplier = -3.18f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 40.0f,
            color = "#f0f8ff"
        )

        val TEQUILA = Drink(
            id = 63,
            name = "Tequila",
            icon = "🥃",
            hydrationMultiplier = -3.18f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 40.0f,
            color = "#f5f5dc"
        )

        val COGNAC = Drink(
            id = 64,
            name = "Cognac",
            icon = "🥃",
            hydrationMultiplier = -3.18f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 40.0f,
            color = "#8b4513"
        )

        val VERMOUTH = Drink(
            id = 65,
            name = "Vermouth",
            icon = "🍸",
            hydrationMultiplier = -0.95f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 18.0f,
            color = "#8b0000"
        )

        val MULLED_WINE = Drink(
            id = 66,
            name = "Mulled Wine",
            icon = "🍷",
            hydrationMultiplier = -0.95f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 10.0f,
            color = "#a52a2a"
        )

        // Коктейли
        val MARGARITA = Drink(
            id = 67,
            name = "Margarita",
            icon = "🍹",
            hydrationMultiplier = -0.26f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 15.0f,
            color = "#00ff7f"
        )

        val MOJITO = Drink(
            id = 68,
            name = "Mojito",
            icon = "🍹",
            hydrationMultiplier = -0.2f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 12.0f,
            color = "#98ff98"
        )

        val BLOODY_MARY = Drink(
            id = 69,
            name = "Bloody Mary",
            icon = "🍹",
            hydrationMultiplier = -0.2f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 12.0f,
            color = "#dc143c"
        )

        val COSMOPOLITAN = Drink(
            id = 70,
            name = "Cosmopolitan",
            icon = "🍸",
            hydrationMultiplier = -0.25f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 18.0f,
            color = "#ff1493"
        )

        val OLD_FASHIONED = Drink(
            id = 71,
            name = "Old Fashioned",
            icon = "🥃",
            hydrationMultiplier = -0.3f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 30.0f,
            color = "#d2691e"
        )

        val MANHATTAN = Drink(
            id = 72,
            name = "Manhattan",
            icon = "🍸",
            hydrationMultiplier = -0.2f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 30.0f,
            color = "#8b0000"
        )

        val GIN_TONIC = Drink(
            id = 73,
            name = "Gin & Tonic",
            icon = "🍸",
            hydrationMultiplier = -0.2f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 10.0f,
            color = "#f0f8ff"
        )

        val WHISKEY_SOUR = Drink(
            id = 74,
            name = "Whiskey Sour",
            icon = "🍸",
            hydrationMultiplier = -0.25f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 20.0f,
            color = "#f4a460"
        )

        val DAIQUIRI = Drink(
            id = 75,
            name = "Daiquiri",
            icon = "🍹",
            hydrationMultiplier = -0.2f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 15.0f,
            color = "#ffe4e1"
        )

        val MAI_TAI = Drink(
            id = 76,
            name = "Mai Tai",
            icon = "🍹",
            hydrationMultiplier = -0.2f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 20.0f,
            color = "#ff8c00"
        )

        val LONG_ISLAND = Drink(
            id = 77,
            name = "Long Island Iced Tea",
            icon = "🍹",
            hydrationMultiplier = -0.2f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 22.0f,
            color = "#d2691e"
        )

        val NEGRONI = Drink(
            id = 78,
            name = "Negroni",
            icon = "🍸",
            hydrationMultiplier = -0.3f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 24.0f,
            color = "#dc143c"
        )

        val MIMOSA = Drink(
            id = 79,
            name = "Mimosa",
            icon = "🥂",
            hydrationMultiplier = -0.23f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 6.0f,
            color = "#ffa500"
        )

        val ESPRESSO_MARTINI = Drink(
            id = 80,
            name = "Espresso Martini",
            icon = "🍸",
            hydrationMultiplier = -0.9f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 25.0f,
            caffeineContent = 60,
            color = "#3e2723"
        )

        val CAESAR = Drink(
            id = 81,
            name = "Bloody Caesar",
            icon = "🍹",
            hydrationMultiplier = -0.2f,
            category = DrinkType.ALCOHOL,
            alcoholPercentage = 12.0f,
            color = "#cd5c5c"
        )

        // === ДРУГИЕ (OTHER) ===
        val SOUP = Drink(
            id = 82,
            name = "Soup",
            icon = "🍲",
            hydrationMultiplier = 0.6f,
            category = DrinkType.OTHER,
            color = "#ff8c00"
        )

        val BONE_BROTH = Drink(
            id = 83,
            name = "Bone Broth",
            icon = "🍲",
            hydrationMultiplier = 0.75f,
            category = DrinkType.OTHER,
            color = "#d2691e"
        )

        val BROTH = Drink(
            id = 84,
            name = "Broth",
            icon = "🍲",
            hydrationMultiplier = 0.75f,
            category = DrinkType.OTHER,
            color = "#daa520"
        )

        val HOT_CHOCOLATE = Drink(
            id = 85,
            name = "Hot Chocolate",
            icon = "☕",
            hydrationMultiplier = 0.6f,
            category = DrinkType.OTHER,
            caffeineContent = 5,
            color = "#8b4513"
        )

        val KOMBUCHA = Drink(
            id = 86,
            name = "Kombucha",
            icon = "🫙",
            hydrationMultiplier = 0.7f,
            category = DrinkType.OTHER,
            color = "#f0e68c"
        )

        val BUBBLE_TEA = Drink(
            id = 87,
            name = "Bubble Tea",
            icon = "🧋",
            hydrationMultiplier = 0.85f,
            category = DrinkType.OTHER,
            color = "#ffc0cb"
        )

        val SOJU = Drink(
            id = 88,
            name = "Soju",
            icon = "🥃",
            hydrationMultiplier = -1.0f,
            category = DrinkType.OTHER,
            alcoholPercentage = 20.0f,
            color = "#e0e0e0"
        )

        val COLA_BREW = Drink(
            id = 89,
            name = "Cola Brew",
            icon = "🥤",
            hydrationMultiplier = 0.8f,
            category = DrinkType.OTHER,
            color = "#3e2723"
        )

        val SYRUP = Drink(
            id = 90,
            name = "Syrup",
            icon = "🍯",
            hydrationMultiplier = -1.0f,
            category = DrinkType.OTHER,
            color = "#daa520"
        )

        val NON_ALCOHOLIC_BEER = Drink(
            id = 91,
            name = "Non-Alcoholic Beer",
            icon = "🍺",
            hydrationMultiplier = 0.7f,
            category = DrinkType.OTHER,
            color = "#f5deb3"
        )

        val NON_ALCOHOLIC_WINE = Drink(
            id = 92,
            name = "Non-Alcoholic Wine",
            icon = "🍷",
            hydrationMultiplier = 0.7f,
            category = DrinkType.OTHER,
            color = "#9370db"
        )

        // === БРЕНДЫ (BRANDS) ===
        val COCA_COLA = Drink(
            id = 93,
            name = "Coca Cola",
            icon = "🥤",
            hydrationMultiplier = 0.83f,
            category = DrinkType.BRANDS,
            caffeineContent = 34,
            color = "#dc143c"
        )

        val COLA_ZERO = Drink(
            id = 94,
            name = "Coca Cola Zero",
            icon = "🥤",
            hydrationMultiplier = 0.86f,
            category = DrinkType.BRANDS,
            caffeineContent = 34,
            color = "#000000"
        )

        val PEPSI = Drink(
            id = 95,
            name = "Pepsi",
            icon = "🥤",
            hydrationMultiplier = 0.83f,
            category = DrinkType.BRANDS,
            caffeineContent = 38,
            color = "#004b93"
        )

        val SPRITE = Drink(
            id = 96,
            name = "Sprite",
            icon = "🥤",
            hydrationMultiplier = 0.87f,
            category = DrinkType.BRANDS,
            color = "#00ff00"
        )

        val FANTA = Drink(
            id = 97,
            name = "Fanta",
            icon = "🥤",
            hydrationMultiplier = 0.87f,
            category = DrinkType.BRANDS,
            color = "#ff8c00"
        )

        val MOUNTAIN_DEW = Drink(
            id = 98,
            name = "Mountain Dew",
            icon = "🥤",
            hydrationMultiplier = 0.83f,
            category = DrinkType.BRANDS,
            caffeineContent = 54,
            color = "#c2e812"
        )

        val DR_PEPPER = Drink(
            id = 99,
            name = "Dr Pepper",
            icon = "🥤",
            hydrationMultiplier = 0.83f,
            category = DrinkType.BRANDS,
            caffeineContent = 41,
            color = "#8b0000"
        )

        val RED_BULL = Drink(
            id = 100,
            name = "Red Bull",
            icon = "⚡",
            hydrationMultiplier = 0.55f,
            category = DrinkType.BRANDS,
            caffeineContent = 80,
            color = "#1e3a8a"
        )

        val MONSTER = Drink(
            id = 101,
            name = "Monster Energy",
            icon = "⚡",
            hydrationMultiplier = 0.5f,
            category = DrinkType.BRANDS,
            caffeineContent = 86,
            color = "#00ff00"
        )

        /**
         * Получить все предустановленные напитки
         */
        fun getDefaultDrinks(): List<Drink> = listOf(
            // Вода (2)
            WATER, MINERAL_WATER,

            // Чай (11)
            HERBAL_TEA, GREEN_TEA, BLACK_TEA, WHITE_TEA, OOLONG_TEA,
            FRUIT_TEA, DECAF_TEA, CHAI_LATTE, ROYAL_MILK_TEA,
            CHRYSANTHEMUM_TEA, BARLEY_TEA,

            // Кофе (10)
            COFFEE, ESPRESSO, AMERICANO, LATTE, CAPPUCCINO,
            MOCHA, MACCHIATO, FLAT_WHITE, DECAF_COFFEE, CHICORY,

            // Молочные (8)
            MILK, SKIM_MILK, ALMOND_MILK, SOY_MILK, OAT_MILK,
            KEFIR, YOGURT, MILKSHAKE,

            // Фруктовые (7)
            JUICE, ORANGE_JUICE, COCONUT_WATER, LEMONADE,
            APPLE_SPRITZ, SMOOTHIE, ALOE_VERA_DRINK,

            // Активные (3)
            SPORTS_DRINK, ENERGY_DRINK, PROTEIN_SHAKE,

            // Газировка (5)
            SODA, DIET_SODA, GINGER_ALE, ROOT_BEER, APEROL,

            // Моктейли (8)
            ARNOLD_PALMER, SHIRLEY_TEMPLE, VIRGIN_MOJITO,
            VIRGIN_BLOODY_MARY, VIRGIN_MARGARITA, VIRGIN_PINA_COLADA,
            VIRGIN_COSMOPOLITAN, VIRGIN_OLD_FASHIONED,

            // Алкоголь (27)
            BEER_LIGHT, BEER_REGULAR, RED_WINE, WHITE_WINE, CHAMPAGNE,
            VODKA, WHISKEY, GIN, TEQUILA, COGNAC, VERMOUTH, MULLED_WINE,
            MARGARITA, MOJITO, BLOODY_MARY, COSMOPOLITAN, OLD_FASHIONED,
            MANHATTAN, GIN_TONIC, WHISKEY_SOUR, DAIQUIRI, MAI_TAI,
            LONG_ISLAND, NEGRONI, MIMOSA, ESPRESSO_MARTINI, CAESAR,

            // Другие (11)
            SOUP, BONE_BROTH, BROTH, HOT_CHOCOLATE, KOMBUCHA,
            BUBBLE_TEA, SOJU, COLA_BREW, SYRUP,
            NON_ALCOHOLIC_BEER, NON_ALCOHOLIC_WINE,

            // Бренды (9)
            COCA_COLA, COLA_ZERO, PEPSI, SPRITE, FANTA,
            MOUNTAIN_DEW, DR_PEPPER, RED_BULL, MONSTER
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

/**
 * Типы напитков (категории)
 */
enum class DrinkType(val displayName: String, val icon: String, val order: Int) {
    WATER("Water", "💧", 0),
    TEA("Tea", "🍵", 1),
    COFFEE("Coffee", "☕", 2),
    DAIRY("Dairy & Plant-Based", "🥛", 3),
    JUICES("Juices & Smoothies", "🧃", 4),
    SPORTS("Sports & Energy", "⚡", 5),
    SOFT_DRINKS("Soft Drinks", "🥤", 6),
    MOCKTAILS("Mocktails", "🍹", 7),
    ALCOHOL("Alcohol", "🍺", 8),
    OTHER("Other", "🫙", 9),
    BRANDS("Brands", "🏷️", 10),
    CUSTOM("Custom", "✨", 11);

    companion object {
        /**
         * Получить все категории в правильном порядке
         */
        fun getAllOrdered(): List<DrinkType> {
            return values().sortedBy { it.order }
        }

        /**
         * Получить категорию по имени
         */
        fun fromString(value: String): DrinkType {
            return values().find { it.name == value } ?: CUSTOM
        }
    }
}