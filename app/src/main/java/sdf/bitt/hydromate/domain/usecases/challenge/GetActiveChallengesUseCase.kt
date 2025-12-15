package sdf.bitt.hydromate.domain.usecases.challenge

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.Challenge
import sdf.bitt.hydromate.domain.repositories.ChallengeRepository
import javax.inject.Inject

class GetActiveChallengesUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    operator fun invoke(): Flow<List<Challenge>> {
        return repository.getActiveChallengesFlow()
    }
}
