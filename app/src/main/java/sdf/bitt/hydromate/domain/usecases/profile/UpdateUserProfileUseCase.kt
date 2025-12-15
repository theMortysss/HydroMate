package sdf.bitt.hydromate.domain.usecases.profile

import sdf.bitt.hydromate.domain.entities.UserProfile
import sdf.bitt.hydromate.domain.repositories.ProfileRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile): Result<Unit> {
        return repository.updateUserProfile(profile)
    }
}
