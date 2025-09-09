package sdf.bitt.hydromate.domain.usecases

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.UserSettings
import sdf.bitt.hydromate.domain.repositories.UserSettingsRepository
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val repository: UserSettingsRepository
) {
    operator fun invoke(): Flow<UserSettings> {
        return repository.getUserSettings()
    }
}