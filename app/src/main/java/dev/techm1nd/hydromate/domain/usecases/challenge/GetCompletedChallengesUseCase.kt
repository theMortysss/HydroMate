package dev.techm1nd.hydromate.domain.usecases.challenge

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.Challenge
import dev.techm1nd.hydromate.domain.repositories.ChallengeRepository
import javax.inject.Inject

class GetCompletedChallengesUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    operator fun invoke(): Flow<List<Challenge>> {
        return repository.getCompletedChallengesFlow()
    }
}
