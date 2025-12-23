package dev.techm1nd.hydromate.domain.entities

import kotlinx.serialization.Serializable

/**
 * Preset для быстрого добавления воды
 */
@Serializable
data class QuickAddPreset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val amount: Int,
    val drinkId: Long,
    val drinkName: String = "",
    val drinkIcon: String = "💧",
    val order: Int = 0
) {
    /**
     * Отображаемое имя для UI
     */
    val displayName: String
        get() = "${amount}ml $drinkIcon"

    companion object {
        /**
         * Дефолтные пресеты (только вода)
         */
        fun getDefaults(): List<QuickAddPreset> = listOf(
            QuickAddPreset(
                amount = 250,
                drinkId = 1,
                drinkName = "Water",
                drinkIcon = "💧",
                order = 0
            ),
            QuickAddPreset(
                amount = 500,
                drinkId = 1,
                drinkName = "Water",
                drinkIcon = "💧",
                order = 1
            ),
            QuickAddPreset(
                amount = 750,
                drinkId = 1,
                drinkName = "Water",
                drinkIcon = "💧",
                order = 2
            )
        )
    }
}