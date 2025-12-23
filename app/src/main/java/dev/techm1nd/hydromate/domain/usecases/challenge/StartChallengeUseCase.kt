package dev.techm1nd.hydromate.domain.usecases.challenge

import dev.techm1nd.hydromate.domain.entities.Challenge
import dev.techm1nd.hydromate.domain.entities.ChallengeType
import dev.techm1nd.hydromate.domain.repositories.ChallengeRepository
import java.time.LocalDate
import javax.inject.Inject

class StartChallengeUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(
        type: ChallengeType,
        startDate: LocalDate = LocalDate.now()
    ): Result<Challenge> {
        // Проверка на активные челленджи того же типа
        val activeChallenge = repository.getActiveChallengeOfType(type)
        if (activeChallenge != null) {
            return Result.failure(
                IllegalStateException("You already have an active ${type.displayName} challenge")
            )
        }

        val challenge = Challenge.create(type, startDate)
        return repository.insertChallenge(challenge)
    }
}