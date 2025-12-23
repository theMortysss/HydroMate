package dev.techm1nd.hydromate.domain.usecases.achievement

import kotlinx.coroutines.flow.first
import dev.techm1nd.hydromate.domain.entities.Achievement
import dev.techm1nd.hydromate.domain.repositories.AchievementRepository
import dev.techm1nd.hydromate.domain.repositories.ProfileRepository
import dev.techm1nd.hydromate.domain.usecases.profile.AddXPUseCase
import java.time.LocalDateTime
import javax.inject.Inject

class UnlockAchievementUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val profileRepository: ProfileRepository,
    private val addXPUseCase: AddXPUseCase
) {
    suspend operator fun invoke(achievementId: String): Result<Achievement> {
        val achievement = achievementRepository.getAchievementById(achievementId)
            ?: return Result.failure(IllegalArgumentException("Achievement not found"))

        if (achievement.isUnlocked) {
            return Result.success(achievement)
        }

        val unlockedAchievement = achievement.copy(
            isUnlocked = true,
            unlockedAt = LocalDateTime.now()
        )

        achievementRepository.updateAchievement(unlockedAchievement)

        // Добавляем XP
        addXPUseCase(achievement.xpReward)

        // Разблокируем персонажа, если есть
        achievement.unlockableCharacter?.let { character ->
            addXPUseCase.unlockCharacter(character)
        }

        updateProfileAchievementCounter()

        return Result.success(unlockedAchievement)
    }

    private suspend fun updateProfileAchievementCounter() {
        try {
            val currentProfile = profileRepository.getUserProfile().first()
            val allAchievements = achievementRepository.getAllAchievements().first()
            val unlockedCount = allAchievements.count { it.isUnlocked }

            val updatedProfile = currentProfile.copy(
                achievementsUnlocked = unlockedCount
            )

            profileRepository.updateUserProfile(updatedProfile)
            android.util.Log.d("UnlockAchievement", "Updated achievement counter: $unlockedCount")
        } catch (e: Exception) {
            android.util.Log.e("UnlockAchievement", "Failed to update counter", e)
        }
    }
}
