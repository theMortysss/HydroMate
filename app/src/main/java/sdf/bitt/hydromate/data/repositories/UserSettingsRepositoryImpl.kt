package sdf.bitt.hydromate.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sdf.bitt.hydromate.data.local.dao.UserSettingsDao
import sdf.bitt.hydromate.data.mappers.UserSettingsMapper
import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.UserSettings
import sdf.bitt.hydromate.domain.repositories.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) : UserSettingsRepository {

    override fun getUserSettings(): Flow<UserSettings> {
        return userSettingsDao.getUserSettings()
            .map { UserSettingsMapper.toDomain(it) }
    }

    override suspend fun updateUserSettings(settings: UserSettings): Result<Unit> {
        return try {
            val entity = UserSettingsMapper.toEntity(settings)
            userSettingsDao.insertOrUpdateSettings(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDailyGoal(goal: Int): Result<Unit> {
        return try {
            userSettingsDao.updateDailyGoal(goal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSelectedCharacter(character: CharacterType): Result<Unit> {
        return try {
            userSettingsDao.updateSelectedCharacter(character.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            userSettingsDao.updateNotificationsEnabled(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
