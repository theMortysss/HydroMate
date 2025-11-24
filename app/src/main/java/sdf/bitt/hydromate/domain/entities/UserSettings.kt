package sdf.bitt.hydromate.domain.entities

import java.time.LocalTime

data class UserSettings(
    val dailyGoal: Int = 2000, // мл
    val selectedCharacter: CharacterType = CharacterType.PENGUIN,
    val notificationsEnabled: Boolean = true,
    val notificationInterval: Int = 60, // минут
    val wakeUpTime: LocalTime = LocalTime.of(8, 0),
    val bedTime: LocalTime = LocalTime.of(22, 0),
    val quickAmounts: List<Int> = listOf(250, 500, 750),
    val hydrationThreshold: Float = 1.0f, // Порог гидратации (0.8 - 1.2)
    val showNetHydration: Boolean = true // Показывать чистую гидратацию или общий объем
)

enum class CharacterType(val displayName: String, val emoji: String) {
    PENGUIN("Penguin", "🐧"),
    CAT("Cat", "🐱"),
    FROG("Frog", "🐸"),
    DUCK("Duck", "🦆"),
    FISH("Fish", "🐠")
}