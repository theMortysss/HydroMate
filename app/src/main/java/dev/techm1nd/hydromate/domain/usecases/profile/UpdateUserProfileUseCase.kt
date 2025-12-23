package dev.techm1nd.hydromate.domain.usecases.profile

import dev.techm1nd.hydromate.domain.entities.UserProfile
import dev.techm1nd.hydromate.domain.repositories.ProfileRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile): Result<Unit> {
        return repository.updateUserProfile(profile)
    }
}
