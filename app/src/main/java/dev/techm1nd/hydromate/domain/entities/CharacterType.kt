package dev.techm1nd.hydromate.domain.entities

enum class CharacterType(
    val displayName: String,
    val emoji: String,
    val description: String,
    val unlockRequirement: String,
    val isUnlockedByDefault: Boolean = false
) {
    PENGUIN(
        displayName = "Penguin",
        emoji = "🐧",
        description = "Your first companion",
        unlockRequirement = "Available from start",
        isUnlockedByDefault = true
    ),
    CAT(
        displayName = "Cat",
        emoji = "🐱",
        description = "Independent and curious",
        unlockRequirement = "Complete caffeine-free challenge"
    ),
    FROG(
        displayName = "Frog",
        emoji = "🐸",
        description = "Always happy and hydrated",
        unlockRequirement = "Complete alcohol-free challenge"
    ),
    DUCK(
        displayName = "Duck",
        emoji = "🦆",
        description = "Loves pure water",
        unlockRequirement = "Complete water-only challenge"
    ),
    FISH(
        displayName = "Fish",
        emoji = "🐠",
        description = "Master of consistency",
        unlockRequirement = "30-day streak"
    ),
    UNICORN(
        displayName = "Unicorn",
        emoji = "🦄",
        description = "Legendary perfectionist",
        unlockRequirement = "Perfect month"
    ),
    DRAGON(
        displayName = "Dragon",
        emoji = "🐉",
        description = "Mythical hydration expert",
        unlockRequirement = "Drink 100 liters total"
    ),
    CHAMELEON(
        displayName = "Chameleon",
        emoji = "🦎",
        description = "Loves variety",
        unlockRequirement = "Try 20 different drinks"
    )
}
