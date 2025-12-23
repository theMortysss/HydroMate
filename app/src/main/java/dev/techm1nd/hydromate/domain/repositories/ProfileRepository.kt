package dev.techm1nd.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.UserProfile

interface ProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
}
