package dev.techm1nd.hydromate.domain.usecases.challenge

import kotlinx.coroutines.flow.first
import dev.techm1nd.hydromate.domain.entities.Achievement
import dev.techm1nd.hydromate.domain.entities.Challenge
import dev.techm1nd.hydromate.domain.entities.ChallengeType
import dev.techm1nd.hydromate.domain.repositories.AchievementRepository
import dev.techm1nd.hydromate.domain.repositories.ChallengeRepository
import dev.techm1nd.hydromate.domain.repositories.ProfileRepository
import dev.techm1nd.hydromate.domain.usecases.achievement.UnlockAchievementUseCase
import dev.techm1nd.hydromate.domain.usecases.profile.AddXPUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class CompleteChallengeUseCase @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val achievementRepository: AchievementRepository,
    private val profileRepository: ProfileRepository,
    private val unlockAchievementUseCase: UnlockAchievementUseCase,
    private val addXPUseCase: AddXPUseCase
) {
    suspend operator fun invoke(challengeId: String): Result<CompletionResult> {
        val challenge = challengeRepository.getChallengeById(challengeId)
            ?: return Result.failure(IllegalArgumentException("Challenge not found"))

        if (!challenge.isActive) {
            return Result.failure(IllegalStateException("Challenge is not active"))
        }

        val today = LocalDate.now()
        if (today.isBefore(challenge.endDate)) {
            return Result.failure(IllegalStateException("Challenge is not yet completed"))
        }

        // Mark challenge as completed
        val completedChallenge = challenge.copy(
            isActive = false,
            isCompleted = true
        )
        challengeRepository.updateChallenge(completedChallenge)

        // Add XP
        addXPUseCase(challenge.xpReward)

        // NEW: Update profile challenge counter
        updateProfileChallengeCounter()

//        // Check and unlock achievement
//        val unlockedAchievement = unlockChallengeAchievement(challenge)
        val unlockedAchievement = checkAndUnlockAchievement(challenge)

        return Result.success(
            CompletionResult(
                challenge = completedChallenge,
                xpGained = challenge.xpReward,
                achievementUnlocked = unlockedAchievement
            )
        )
    }

    private suspend fun checkAndUnlockAchievement(challenge: Challenge): Achievement? {
        // Only unlock if duration is at least 14 days (matching achievement descriptions)
        if (challenge.durationDays < 14) return null

        val achievementId = when (challenge.type) {
            ChallengeType.NO_CAFFEINE -> "challenge_caffeine_14"
            ChallengeType.NO_ALCOHOL -> "challenge_alcohol_14"
            ChallengeType.WATER_ONLY -> "challenge_water_14"
            ChallengeType.NO_LACTOSE -> "challenge_lactose_14"
            ChallengeType.NO_SUGAR -> "challenge_sugar_14"
            ChallengeType.NO_SODA -> "challenge_soda_14"
            ChallengeType.PLANT_BASED -> "challenge_plant_14"
            ChallengeType.HYDRATION_HERO -> "challenge_hero_14"
        }

        val achievement = achievementRepository.getAchievementById(achievementId)
        if (achievement != null && !achievement.isUnlocked) {
            return unlockAchievementUseCase(achievementId).getOrNull()
        }
        return null
    }

    private suspend fun unlockChallengeAchievement(challenge: Challenge): Achievement? {
        val achievementId = "challenge_${challenge.type.name.lowercase()}_${challenge.durationDays}"
        val achievement = achievementRepository.getAchievementById(achievementId)
            ?: return null

        if (achievement.isUnlocked) return null

        val unlockedAchievement = achievement.copy(
            isUnlocked = true,
            unlockedAt = LocalDateTime.now()
        )

        achievementRepository.updateAchievement(unlockedAchievement)

        // Unlock character if available
        unlockedAchievement.unlockableCharacter?.let { character ->
            addXPUseCase.unlockCharacter(character)
        }

        return unlockedAchievement
    }

    private suspend fun updateProfileChallengeCounter() {
        try {
            val currentProfile = profileRepository.getUserProfile().first()
            val allChallenges = challengeRepository.getAllChallenges()
            val completedCount = allChallenges.count { it.isCompleted }

            val updatedProfile = currentProfile.copy(
                challengesCompleted = completedCount
            )

            profileRepository.updateUserProfile(updatedProfile)
            android.util.Log.d("CompleteChallenge", "Updated challenge counter: $completedCount")
        } catch (e: Exception) {
            android.util.Log.e("CompleteChallenge", "Failed to update counter", e)
        }
    }

    data class CompletionResult(
        val challenge: Challenge,
        val xpGained: Int,
        val achievementUnlocked: Achievement?
    )
}
