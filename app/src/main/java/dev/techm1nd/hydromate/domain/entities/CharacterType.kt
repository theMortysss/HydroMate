package dev.techm1nd.hydromate.domain.entities

enum class CharacterType(
    val displayName: String,
    val emoji: String,
    val description: String,
    val unlockRequirement: String,
    val isUnlockedByDefault: Boolean = false
) {
    PENGUIN(
        displayName = "Пингвин",
        emoji = "🐧",
        description = "Ваш первый спутник",
        unlockRequirement = "Доступен с самого начала",
        isUnlockedByDefault = true
    ),
    CAT(
        displayName = "Кот",
        emoji = "🐱",
        description = "Независимый и любопытный",
        unlockRequirement = "Завершите испытание без кофеина"
    ),
    FROG(
        displayName = "Лягушка",
        emoji = "🐸",
        description = "Всегда счастлива и поддерживает водный баланс",
        unlockRequirement = "Завершите испытание без алкоголя"
    ),
    DUCK(
        displayName = "Утка",
        emoji = "🦆",
        description = "Любит чистую воду",
        unlockRequirement = "Завершите испытание только с водой"
    ),
    FISH(
        displayName = "Рыбка",
        emoji = "🐠",
        description = "Мастер постоянства",
        unlockRequirement = "Серия из 30 дней"
    ),
    UNICORN(
        displayName = "Единорог",
        emoji = "🦄",
        description = "Легендарный перфекционист",
        unlockRequirement = "Идеальный месяц"
    ),
    DRAGON(
        displayName = "Дракон",
        emoji = "🐉",
        description = "Мифический эксперт по гидратации",
        unlockRequirement = "Выпейте всего 100 литров"
    ),
    CHAMELEON(
        displayName = "Хамелеон",
        emoji = "🦎",
        description = "Любит разнообразие",
        unlockRequirement = "Попробуйте 20 разных напитков"
    )
}