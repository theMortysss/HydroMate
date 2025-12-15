package sdf.bitt.hydromate.domain.usecases.challenge

import sdf.bitt.hydromate.domain.entities.Challenge
import sdf.bitt.hydromate.domain.entities.ChallengeViolation
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.repositories.ChallengeRepository
import java.time.LocalDate
import javax.inject.Inject

class CheckChallengeViolationUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(drink: Drink): Result<List<Challenge>> {
        val activeChallenges = repository.getActiveChallenges()
        val today = LocalDate.now()
        val violatedChallenges = mutableListOf<Challenge>()

        activeChallenges.forEach { challenge ->
            if (challenge.type.isViolated(drink)) {
                // Use the new factory method with proper serialization
                val violation = ChallengeViolation.create(
                    date = today,
                    drinkName = drink.name,
                    drinkIcon = drink.icon
                )

                val updatedChallenge = challenge.copy(
                    isActive = false,
                    violations = challenge.violations + violation
                )

                repository.updateChallenge(updatedChallenge)
                violatedChallenges.add(updatedChallenge)
            }
        }

        return Result.success(violatedChallenges)
    }
}
