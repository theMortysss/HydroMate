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
                title = "Друг-Пингвин",
                description = "Ваш первый спутник",
                icon = "🐧",
                xpReward = 0,
                isUnlocked = true,
                unlockableCharacter = CharacterType.PENGUIN
            ),

            // Челленджи
            Achievement(
                id = "challenge_caffeine_14",
                type = AchievementType.CHALLENGE_CAFFEINE_FREE_COMPLETED,
                title = "Покоритель кофеина",
                description = "Пройдите 14-дневный челлендж без кофеина",
                icon = "☕",
                xpReward = 200,
                unlockableCharacter = CharacterType.CAT
            ),
            Achievement(
                id = "challenge_alcohol_14",
                type = AchievementType.CHALLENGE_ALCOHOL_FREE_COMPLETED,
                title = "Трезвый супергерой",
                description = "Пройдите 14-дневный челлендж без алкоголя",
                icon = "🍺",
                xpReward = 300,
                unlockableCharacter = CharacterType.FROG
            ),
            Achievement(
                id = "challenge_water_14",
                type = AchievementType.CHALLENGE_WATER_ONLY_COMPLETED,
                title = "Чистая гидратация",
                description = "Пройдите 14-дневный челлендж только на воде",
                icon = "💧",
                xpReward = 300,
                unlockableCharacter = CharacterType.DUCK
            ),
            Achievement(
                id = "challenge_lactose_14",
                type = AchievementType.CHALLENGE_LACTOSE_FREE_COMPLETED,
                title = "Освободитель лактозы",
                description = "Пройдите 14-дневный челлендж без лактозы",
                icon = "🥛",
                xpReward = 150
            ),
            Achievement(
                id = "challenge_sugar_14",
                type = AchievementType.CHALLENGE_SUGAR_FREE_COMPLETED,
                title = "Уничтожитель сахара",
                description = "Пройдите 14-дневный челлендж без сахара",
                icon = "🍬",
                xpReward = 200
            ),
            Achievement(
                id = "challenge_soda_14",
                type = AchievementType.CHALLENGE_SODA_FREE_COMPLETED,
                title = "Выживший без газировки",
                description = "Пройдите 14-дневный челлендж без газировки",
                icon = "🥤",
                xpReward = 150
            ),
            Achievement(
                id = "challenge_plant_14",
                type = AchievementType.CHALLENGE_PLANT_BASED_COMPLETED,
                title = "Сила растений",
                description = "Пройдите 14-дневный челлендж растительных напитков",
                icon = "🌱",
                xpReward = 200
            ),
            Achievement(
                id = "challenge_hero_14",
                type = AchievementType.CHALLENGE_HYDRATION_HERO_COMPLETED,
                title = "Герой гидратации",
                description = "Достигайте дневной цели 14 дней подряд",
                icon = "🏆",
                xpReward = 250
            ),

            // Серии
            Achievement(
                id = "streak_7",
                type = AchievementType.STREAK_7,
                title = "Воин недели",
                description = "Достигайте цели 7 дней подряд",
                icon = "🔥",
                xpReward = 100,
                progressMax = 7
            ),
            Achievement(
                id = "streak_30",
                type = AchievementType.STREAK_30,
                title = "Мастер месяца",
                description = "Достигайте цели 30 дней подряд",
                icon = "🔥",
                xpReward = 500,
                progressMax = 30,
                unlockableCharacter = CharacterType.FISH
            ),

            // Перфект
            Achievement(
                id = "perfect_week",
                type = AchievementType.PERFECT_WEEK,
                title = "Идеальная неделя",
                description = "Достигайте цели каждый день в течение недели",
                icon = "⭐",
                xpReward = 150,
                progressMax = 7
            ),
            Achievement(
                id = "perfect_month",
                type = AchievementType.PERFECT_MONTH,
                title = "Идеальный месяц",
                description = "Достигайте цели каждый день в течение месяца",
                icon = "🌟",
                xpReward = 600,
                progressMax = 30,
                unlockableCharacter = CharacterType.UNICORN
            ),

            // Объемы
            Achievement(
                id = "total_10000ml",
                type = AchievementType.TOTAL_10000ML,
                title = "Новичок гидратации",
                description = "Выпейте 10 литров всего",
                icon = "💧",
                xpReward = 100,
                progressMax = 10000
            ),
            Achievement(
                id = "total_100000ml",
                type = AchievementType.TOTAL_100000ML,
                title = "Эксперт гидратации",
                description = "Выпейте 100 литров всего",
                icon = "💎",
                xpReward = 500,
                progressMax = 100000,
                unlockableCharacter = CharacterType.DRAGON
            ),

            // Специальные
            Achievement(
                id = "early_bird",
                type = AchievementType.EARLY_BIRD,
                title = "Жаворонок",
                description = "Пейте воду в течение часа после пробуждения 10 раз",
                icon = "🌅",
                xpReward = 100,
                progressMax = 10
            ),
            Achievement(
                id = "variety_master",
                type = AchievementType.VARIETY_MASTER,
                title = "Мастер разнообразия",
                description = "Попробуйте 20 разных напитков",
                icon = "🎨",
                xpReward = 200,
                progressMax = 20,
                unlockableCharacter = CharacterType.CHAMELEON
            )
        )
    }
}
