package sdf.bitt.hydromate.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sdf.bitt.hydromate.data.local.dao.AchievementDao
import sdf.bitt.hydromate.data.mappers.AchievementMapper
import sdf.bitt.hydromate.domain.entities.Achievement
import sdf.bitt.hydromate.domain.repositories.AchievementRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao
) : AchievementRepository {

    override fun getAllAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAllAchievements()
            .map { entities -> entities.map { AchievementMapper.toDomain(it) } }
    }

    override suspend fun getAchievementById(id: String): Achievement? {
        return achievementDao.getAchievementById(id)
            ?.let { AchievementMapper.toDomain(it) }
    }

    override suspend fun updateAchievement(achievement: Achievement): Result<Unit> {
        return try {
            val entity = AchievementMapper.toEntity(achievement)
            achievementDao.updateAchievement(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initializeAchievements(): Result<Unit> {
        return try {
            val count = achievementDao.getAchievementsCount()
            if (count == 0) {
                val achievements = Achievement.getAllAchievements()
                val entities = achievements.map { AchievementMapper.toEntity(it) }
                achievementDao.insertAchievements(entities)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
