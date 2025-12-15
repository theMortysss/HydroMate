package sdf.bitt.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.UserSettings

interface UserSettingsRepository {

    fun getUserSettings(): Flow<UserSettings>

    suspend fun updateUserSettings(settings: UserSettings): Result<Unit>

    suspend fun updateDailyGoal(goal: Int): Result<Unit>

//    suspend fun updateSelectedCharacter(character: CharacterType): Result<Unit>

    suspend fun updateNotificationsEnabled(enabled: Boolean): Result<Unit>
}