package sdf.bitt.hydromate.domain.entities

import java.time.LocalDate

data class DailyProgress(
    val date: LocalDate,
    val totalAmount: Int, // Общий объем выпитого
    val goalAmount: Int,
    val entries: List<WaterEntry>,
    val streak: Int = 0,
    val achievementUnlocked: Achievement? = null,

    // NEW: Данные гидратации
    val effectiveHydration: Int = totalAmount, // Эффективная гидратация
    val netHydration: Int = totalAmount // Чистая гидратация (с учетом дегидратации)
) {
    /**
     * Процент прогресса в зависимости от настройки showNetHydration
     */
    fun getProgressPercentage(showNetHydration: Boolean): Float {
        val currentAmount = if (showNetHydration) netHydration else totalAmount
        return (currentAmount.toFloat() / goalAmount).coerceAtMost(1f)
    }

    /**
     * Текущее количество для отображения
     */
    fun getCurrentAmount(showNetHydration: Boolean): Int {
        return if (showNetHydration) netHydration else totalAmount
    }

    /**
     * Достигнута ли цель
     */
    fun isGoalReached(showNetHydration: Boolean): Boolean {
        return getCurrentAmount(showNetHydration) >= goalAmount
    }

    /**
     * Оставшееся количество
     */
    fun getRemainingAmount(showNetHydration: Boolean): Int {
        return (goalAmount - getCurrentAmount(showNetHydration)).coerceAtLeast(0)
    }

    // Оставляем для обратной совместимости
    val progressPercentage: Float
        get() = (totalAmount.toFloat() / goalAmount).coerceAtMost(1f)

    val isGoalReached: Boolean
        get() = totalAmount >= goalAmount

    val remainingAmount: Int
        get() = (goalAmount - totalAmount).coerceAtLeast(0)
}