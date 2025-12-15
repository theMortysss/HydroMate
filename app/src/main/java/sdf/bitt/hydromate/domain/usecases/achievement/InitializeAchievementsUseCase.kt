package sdf.bitt.hydromate.domain.usecases.achievement

import sdf.bitt.hydromate.domain.repositories.AchievementRepository
import javax.inject.Inject

/**
 * Инициализирует достижения при первом запуске
 */
class InitializeAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return achievementRepository.initializeAchievements()
    }
}
