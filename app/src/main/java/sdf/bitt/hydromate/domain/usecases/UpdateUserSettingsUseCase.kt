package sdf.bitt.hydromate.domain.usecases

import sdf.bitt.hydromate.domain.entities.UserSettings
import sdf.bitt.hydromate.domain.repositories.UserSettingsRepository
import javax.inject.Inject

class UpdateUserSettingsUseCase @Inject constructor(
    private val repository: UserSettingsRepository
) {
    suspend operator fun invoke(settings: UserSettings): Result<Unit> {
        return repository.updateUserSettings(settings)
    }
}