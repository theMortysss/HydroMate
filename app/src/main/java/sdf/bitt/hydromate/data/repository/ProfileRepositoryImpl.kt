package sdf.bitt.hydromate.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sdf.bitt.hydromate.data.local.dao.UserProfileDao
import sdf.bitt.hydromate.data.mappers.UserProfileMapper
import sdf.bitt.hydromate.domain.entities.UserProfile
import sdf.bitt.hydromate.domain.repositories.ProfileRepository
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
