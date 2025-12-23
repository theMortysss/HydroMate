package dev.techm1nd.hydromate.domain.usecases.achievement

import dev.techm1nd.hydromate.domain.repositories.AchievementRepository
import javax.inject.Inject

/**
 * Обновляет прогресс достижения
 */
class UpdateAchievementProgressUseCase @Inject constructor(
    private val repository: AchievementRepository
) {
    suspend operator fun invoke(achievementId: String, progress: Int): Result<Unit> {
        return try {
            val achievement = repository.getAchievementById(achievementId)
                ?: return Result.failure(IllegalArgumentException("Achievement not found"))

            if (achievement.isUnlocked) {
                return Result.success(Unit)
            }

            val updatedAchievement = achievement.copy(
                progress = progress.coerceIn(0, achievement.progressMax)
            )

            repository.updateAchievement(updatedAchievement)

            // Если прогресс достиг максимума, разблокируем
            if (updatedAchievement.progress >= updatedAchievement.progressMax) {
                // UnlockAchievementUseCase вызовется автоматически через CheckAchievementProgressUseCase
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
