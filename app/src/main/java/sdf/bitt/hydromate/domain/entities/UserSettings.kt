package sdf.bitt.hydromate.domain.entities

import java.time.LocalTime

data class UserSettings(
    val dailyGoal: Int = 2000, // мл
    val selectedCharacter: CharacterType = CharacterType.PENGUIN,
    val notificationsEnabled: Boolean = true,
    val notificationInterval: Int = 60, // минут
    val wakeUpTime: LocalTime = LocalTime.of(8, 0),
    val bedTime: LocalTime = LocalTime.of(22, 0),
    val quickAddPresets: List<QuickAddPreset> = QuickAddPreset.getDefaults(),
    val showNetHydration: Boolean = true,
    val profile: UserProfile = UserProfile()
) {
    @Deprecated("Use quickAddPresets instead")
    val quickAmounts: List<Int>
        get() = quickAddPresets.map { it.amount }

    /**
     * Получить актуальную цель с учетом профиля
     * Если пользователь использует ручную настройку - берем из профиля
     * Иначе используем расчетную цель (dailyGoal)
     */
    fun getEffectiveGoal(): Int {
        return if (profile.isManualGoal) {
            profile.manualGoal
        } else {
            dailyGoal
        }
    }
}

enum class CharacterType(val displayName: String, val emoji: String) {
    PENGUIN("Penguin", "🐧"),
    CAT("Cat", "🐱"),
    FROG("Frog", "🐸"),
    DUCK("Duck", "🦆"),
    FISH("Fish", "🐠")
}