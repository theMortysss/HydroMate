package dev.techm1nd.hydromate.domain.usecases.achievement

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.Achievement
import dev.techm1nd.hydromate.domain.repositories.AchievementRepository
import javax.inject.Inject

class GetAllAchievementsUseCase @Inject constructor(
    private val repository: AchievementRepository
) {
    operator fun invoke(): Flow<List<Achievement>> {
        return repository.getAllAchievements()
    }
}
