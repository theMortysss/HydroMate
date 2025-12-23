package dev.techm1nd.hydromate.domain.usecases

import dev.techm1nd.hydromate.domain.entities.UserSettings
import dev.techm1nd.hydromate.domain.repositories.UserSettingsRepository
import javax.inject.Inject

class UpdateUserSettingsUseCase @Inject constructor(
    private val repository: UserSettingsRepository
) {
    suspend operator fun invoke(settings: UserSettings): Result<Unit> {
        return repository.updateUserSettings(settings)
    }
}