package dev.techm1nd.hydromate.domain.entities

import java.time.LocalDateTime

/**
 * Тип достижения
 */
enum class AchievementType {
    // Челленджи
    CHALLENGE_CAFFEINE_FREE_COMPLETED,
    CHALLENGE_ALCOHOL_FREE_COMPLETED,
    CHALLENGE_WATER_ONLY_COMPLETED,
    CHALLENGE_LACTOSE_FREE_COMPLETED,
    CHALLENGE_SUGAR_FREE_COMPLETED,
    CHALLENGE_SODA_FREE_COMPLETED,
    CHALLENGE_PLANT_BASED_COMPLETED,
    CHALLENGE_HYDRATION_HERO_COMPLETED,

    // Гидратация
    PERFECT_WEEK,
    PERFECT_MONTH,
    STREAK_7,
    STREAK_30,
    STREAK_100,

    // Количество
    TOTAL_1000ML,
    TOTAL_10000ML,
    TOTAL_100000ML,

    // Специальные
    EARLY_BIRD,      // Выпил воду в течение часа после пробуждения
    NIGHT_OWL,       // Выпил воду перед сном
    VARIETY_MASTER,  // Попробовал 20 разных напитков

    // Персонажи
    CHARACTER_UNLOCKED
}

/**
 * Достижение
 */
data class Achievement(
    val id: String,
    val type: AchievementType,
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: LocalDateTime? = null,
    val progress: Int = 0,
    val progressMax: Int = 1,
    val unlockableCharacter: CharacterType? = null
) {
    val progressPercentage: Float
        get() = if (progressMax > 0) (progress.toFloat() / progressMax * 100).coerceIn(0f, 100f)
        else 100f

    companion object {
        /**
         * Все возможные достижения
         */
        fun getAllAchievements(): List<Achievement> = listOf(
            // Базовые персонажи (открыты сразу)
            Achievement(
                id = "char_penguin",
                type = AchievementType.CHARACTER_UNLOCKED,
                title = "Penguin Pal",
                description = "Your first companion",
                icon = "🐧",
                xpReward = 0,
                isUnlocked = true,
                unlockableCharacter = CharacterType.PENGUIN
            ),

            // Челленджи
            Achievement(
                id = "challenge_caffeine_14",
                type = AchievementType.CHALLENGE_CAFFEINE_FREE_COMPLETED,
                title = "Caffeine Conqueror",
                description = "Complete 14-day caffeine-free challenge",
                icon = "☕",
                xpReward = 200,
                unlockableCharacter = CharacterType.CAT
            ),
            Achievement(
                id = "challenge_alcohol_14",
                type = AchievementType.CHALLENGE_ALCOHOL_FREE_COMPLETED,
                title = "Sober Superstar",
                description = "Complete 14-day alcohol-free challenge",
                icon = "🍺",
                xpReward = 300,
                unlockableCharacter = CharacterType.FROG
            ),
            Achievement(
                id = "challenge_water_14",
                type = AchievementType.CHALLENGE_WATER_ONLY_COMPLETED,
                title = "Pure Hydration",
                description = "Complete 14-day water-only challenge",
                icon = "💧",
                xpReward = 300,
                unlockableCharacter = CharacterType.DUCK
            ),
            Achievement(
                id = "challenge_lactose_14",
                type = AchievementType.CHALLENGE_LACTOSE_FREE_COMPLETED,
                title = "Lactose Liberator",
                description = "Complete 14-day lactose-free challenge",
                icon = "🥛",
                xpReward = 150
            ),
            Achievement(
                id = "challenge_sugar_14",
                type = AchievementType.CHALLENGE_SUGAR_FREE_COMPLETED,
                title = "Sugar Slayer",
                description = "Complete 14-day sugar-free challenge",
                icon = "🍬",
                xpReward = 200
            ),
            Achievement(
                id = "challenge_soda_14",
                type = AchievementType.CHALLENGE_SODA_FREE_COMPLETED,
                title = "Soda Survivor",
                description = "Complete 14-day soda-free challenge",
                icon = "🥤",
                xpReward = 150
            ),
            Achievement(
                id = "challenge_plant_14",
                type = AchievementType.CHALLENGE_PLANT_BASED_COMPLETED,
                title = "Plant Power",
                description = "Complete 14-day plant-based drinks challenge",
                icon = "🌱",
                xpReward = 200
            ),
            Achievement(
                id = "challenge_hero_14",
                type = AchievementType.CHALLENGE_HYDRATION_HERO_COMPLETED,
                title = "Hydration Hero",
                description = "Reach daily goal every day for 14 days",
                icon = "🏆",
                xpReward = 250
            ),

            // Серии
            Achievement(
                id = "streak_7",
                type = AchievementType.STREAK_7,
                title = "Week Warrior",
                description = "Reach your goal 7 days in a row",
                icon = "🔥",
                xpReward = 100,
                progressMax = 7
            ),
            Achievement(
                id = "streak_30",
                type = AchievementType.STREAK_30,
                title = "Month Master",
                description = "Reach your goal 30 days in a row",
                icon = "🔥",
                xpReward = 500,
                progressMax = 30,
                unlockableCharacter = CharacterType.FISH
            ),

            // Перфект
            Achievement(
                id = "perfect_week",
                type = AchievementType.PERFECT_WEEK,
                title = "Perfect Week",
                description = "Reach your goal every day for a week",
                icon = "⭐",
                xpReward = 150,
                progressMax = 7
            ),
            Achievement(
                id = "perfect_month",
                type = AchievementType.PERFECT_MONTH,
                title = "Perfect Month",
                description = "Reach your goal every day for a month",
                icon = "🌟",
                xpReward = 600,
                progressMax = 30,
                unlockableCharacter = CharacterType.UNICORN
            ),

            // Объемы
            Achievement(
                id = "total_10000ml",
                type = AchievementType.TOTAL_10000ML,
                title = "Hydration Beginner",
                description = "Drink 10 liters total",
                icon = "💧",
                xpReward = 100,
                progressMax = 10000
            ),
            Achievement(
                id = "total_100000ml",
                type = AchievementType.TOTAL_100000ML,
                title = "Hydration Expert",
                description = "Drink 100 liters total",
                icon = "💎",
                xpReward = 500,
                progressMax = 100000,
                unlockableCharacter = CharacterType.DRAGON
            ),

            // Специальные
            Achievement(
                id = "early_bird",
                type = AchievementType.EARLY_BIRD,
                title = "Early Bird",
                description = "Drink water within an hour of waking up 10 times",
                icon = "🌅",
                xpReward = 100,
                progressMax = 10
            ),
            Achievement(
                id = "variety_master",
                type = AchievementType.VARIETY_MASTER,
                title = "Variety Master",
                description = "Try 20 different drinks",
                icon = "🎨",
                xpReward = 200,
                progressMax = 20,
                unlockableCharacter = CharacterType.CHAMELEON
            )
        )
    }
}
