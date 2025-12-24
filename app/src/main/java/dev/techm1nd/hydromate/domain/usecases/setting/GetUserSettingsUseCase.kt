package dev.techm1nd.hydromate.domain.usecases.setting

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.UserSettings
import dev.techm1nd.hydromate.domain.repositories.UserSettingsRepository
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val repository: UserSettingsRepository
) {
    operator fun invoke(): Flow<UserSettings> {
        return repository.getUserSettings()
    }
}