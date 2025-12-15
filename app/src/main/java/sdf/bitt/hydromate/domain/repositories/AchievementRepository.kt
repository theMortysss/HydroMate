package sdf.bitt.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.Achievement

interface AchievementRepository {
    fun getAllAchievements(): Flow<List<Achievement>>
    suspend fun getAchievementById(id: String): Achievement?
    suspend fun updateAchievement(achievement: Achievement): Result<Unit>
    suspend fun initializeAchievements(): Result<Unit>
}
