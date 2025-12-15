package sdf.bitt.hydromate.domain.usecases.profile

import kotlinx.coroutines.flow.first
import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.UserProfile
import sdf.bitt.hydromate.domain.repositories.ProfileRepository
import javax.inject.Inject

class AddXPUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(xp: Int): Result<LevelUpResult> {
        val currentProfile = repository.getUserProfile().first()
        val oldLevel = currentProfile.level

        val updatedProfile = currentProfile.addXP(xp)
        repository.updateUserProfile(updatedProfile)

        val didLevelUp = updatedProfile.level > oldLevel

        android.util.Log.d("AddXP", "Added $xp XP, Level: ${updatedProfile.level}, Total XP: ${updatedProfile.totalXP}")

        return Result.success(
            LevelUpResult(
                newLevel = updatedProfile.level,
                didLevelUp = didLevelUp,
                xpGained = xp
            )
        )
    }

    suspend fun unlockCharacter(character: CharacterType) {
        val currentProfile = repository.getUserProfile().first()
        val updatedProfile = currentProfile.unlockCharacter(character)
        repository.updateUserProfile(updatedProfile)
        android.util.Log.d("AddXP", "Unlocked character: ${character.displayName}")
    }

    // NEW: Track drinks consumed
    suspend fun incrementDrinkCount(drinkName: String) {
        try {
            val currentProfile = repository.getUserProfile().first()
            val newDrinksTried = if (!currentProfile.uniqueDrinksTried.contains(drinkName)) {
                currentProfile.uniqueDrinksTried + drinkName
            } else {
                currentProfile.uniqueDrinksTried
            }

            val updatedProfile = currentProfile.copy(
                totalDrinksDrank = currentProfile.totalDrinksDrank + 1,
                uniqueDrinksTried = newDrinksTried
            )

            repository.updateUserProfile(updatedProfile)
            android.util.Log.d("AddXP", "Updated drinks: total=${updatedProfile.totalDrinksDrank}, unique=${newDrinksTried.size}")
        } catch (e: Exception) {
            android.util.Log.e("AddXP", "Failed to track drink", e)
        }
    }

    data class LevelUpResult(
        val newLevel: Int,
        val didLevelUp: Boolean,
        val xpGained: Int
    )
}
