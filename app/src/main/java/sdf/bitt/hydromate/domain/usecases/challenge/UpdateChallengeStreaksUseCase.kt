package sdf.bitt.hydromate.domain.usecases.challenge

import sdf.bitt.hydromate.domain.entities.Challenge
import sdf.bitt.hydromate.domain.repositories.ChallengeRepository
import java.time.LocalDate
import javax.inject.Inject

class UpdateChallengeStreaksUseCase @Inject constructor(
    private val repository: ChallengeRepository,
    private val completeChallengeUseCase: CompleteChallengeUseCase
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val today = LocalDate.now()
            val activeChallenges = repository.getActiveChallenges()

            activeChallenges.forEach { challenge ->
                // Calculate days passed including today (assuming no violation yet today)
                val daysPassed = challenge.startDate.until(today).days + 1

                // If there is a violation today, it should have been deactivated already by UpdateChallengeProgressUseCase
                // For active ones, update streak
                val updatedStreak = daysPassed.coerceAtLeast(challenge.currentStreak)

                val updatedChallenge = challenge.copy(currentStreak = updatedStreak)
                repository.updateChallenge(updatedChallenge)

                // Check if completed
                if (updatedStreak >= challenge.durationDays) {
                    completeChallengeUseCase(challenge.id)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}