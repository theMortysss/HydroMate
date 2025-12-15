package sdf.bitt.hydromate.domain.usecases.profile

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.UserProfile
import sdf.bitt.hydromate.domain.repositories.ProfileRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> {
        return repository.getUserProfile()
    }
}
