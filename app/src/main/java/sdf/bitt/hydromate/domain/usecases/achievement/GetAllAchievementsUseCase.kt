package sdf.bitt.hydromate.domain.usecases.achievement

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.Achievement
import sdf.bitt.hydromate.domain.repositories.AchievementRepository
import javax.inject.Inject

class GetAllAchievementsUseCase @Inject constructor(
    private val repository: AchievementRepository
) {
    operator fun invoke(): Flow<List<Achievement>> {
        return repository.getAllAchievements()
    }
}
