package sdf.bitt.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.UserProfile

interface ProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
}
