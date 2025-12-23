package dev.techm1nd.hydromate.domain.usecases.profile

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.UserProfile
import dev.techm1nd.hydromate.domain.repositories.ProfileRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> {
        return repository.getUserProfile()
    }
}
