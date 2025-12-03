package sdf.bitt.hydromate.domain.entities

import java.time.LocalTime

data class UserSettings(
    val dailyGoal: Int = 2000, // мл
    val selectedCharacter: CharacterType = CharacterType.PENGUIN,
    val notificationsEnabled: Boolean = true,
    val notificationInterval: Int = 60, // минут
    val wakeUpTime: LocalTime = LocalTime.of(8, 0),
    val bedTime: LocalTime = LocalTime.of(22, 0),

    // UPDATED: Теперь используем QuickAddPreset вместо простых Int
    val quickAddPresets: List<QuickAddPreset> = QuickAddPreset.getDefaults(),

    val showNetHydration: Boolean = true // Показывать чистую гидратацию или общий объем
) {
    // Обратная совместимость: получить только суммы для старого кода
    @Deprecated("Use quickAddPresets instead")
    val quickAmounts: List<Int>
        get() = quickAddPresets.map { it.amount }
}

enum class CharacterType(val displayName: String, val emoji: String) {
    PENGUIN("Penguin", "🐧"),
    CAT("Cat", "🐱"),
    FROG("Frog", "🐸"),
    DUCK("Duck", "🦆"),
    FISH("Fish", "🐠")
}