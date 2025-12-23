package dev.techm1nd.hydromate.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.techm1nd.hydromate.data.local.dao.UserProfileDao
import dev.techm1nd.hydromate.data.mappers.UserProfileMapper
import dev.techm1nd.hydromate.domain.entities.UserProfile
import dev.techm1nd.hydromate.domain.repositories.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : ProfileRepository {

    override fun getUserProfile(): Flow<UserProfile> {
        return userProfileDao.getUserProfile()
            .map { UserProfileMapper.toDomain(it) }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val entity = UserProfileMapper.toEntity(profile)
            val existing = userProfileDao.getUserProfileSync()

            if (existing == null) {
                userProfileDao.insertProfile(entity)
            } else {
                userProfileDao.updateProfile(entity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
