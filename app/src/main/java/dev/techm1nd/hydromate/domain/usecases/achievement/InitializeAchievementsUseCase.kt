package dev.techm1nd.hydromate.domain.usecases.achievement

import dev.techm1nd.hydromate.domain.repositories.AchievementRepository
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
