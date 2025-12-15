package sdf.bitt.hydromate.domain.usecases.challenge

import sdf.bitt.hydromate.domain.repositories.ChallengeRepository
import javax.inject.Inject

class AbandonChallengeUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(challengeId: String): Result<Unit> {
        val challenge = repository.getChallengeById(challengeId)
            ?: return Result.failure(IllegalArgumentException("Challenge not found"))

        val abandonedChallenge = challenge.copy(
            isActive = false,
            isCompleted = false
        )

        return repository.updateChallenge(abandonedChallenge)
    }
}

