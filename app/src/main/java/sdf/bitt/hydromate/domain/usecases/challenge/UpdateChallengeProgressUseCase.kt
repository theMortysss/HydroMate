package sdf.bitt.hydromate.domain.usecases.challenge

import sdf.bitt.hydromate.domain.entities.ChallengeViolation
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.repositories.ChallengeRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Проверяет и обновляет прогресс активных челленджей при добавлении напитка
 */
class UpdateChallengeProgressUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(drink: Drink): Result<List<String>> {
        return try {
            val activeChallenges = repository.getActiveChallenges()
            val today = LocalDate.now()
            val violatedChallengeIds = mutableListOf<String>()

            activeChallenges.forEach { challenge ->
                if (challenge.type.isViolated(drink)) {
                    // Use the new factory method with proper serialization
                    val violation = ChallengeViolation.create(
                        date = today,
                        drinkName = drink.name,
                        drinkIcon = drink.icon
                    )

                    val violatedChallenge = challenge.copy(
                        isActive = false,
                        isCompleted = false,
                        violations = challenge.violations + violation
                    )

                    repository.updateChallenge(violatedChallenge)
                    violatedChallengeIds.add(challenge.id)
                }
            }

            Result.success(violatedChallengeIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
