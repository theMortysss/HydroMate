package dev.techm1nd.hydromate.domain.usecases

import dev.techm1nd.hydromate.domain.repositories.UserSettingsRepository
import javax.inject.Inject

class UpdateDailyGoalUseCase @Inject constructor(
    private val repository: UserSettingsRepository
) {
    suspend operator fun invoke(goal: Int): Result<Unit> {
        return if (goal > 0) {
            repository.updateDailyGoal(goal)
        } else {
            Result.failure(IllegalArgumentException("Daily goal must be positive"))
        }
    }
}