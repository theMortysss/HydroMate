package dev.techm1nd.hydromate.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.data.local.entities.UserSettingsEntity

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)

    @Query("UPDATE user_settings SET daily_goal = :goal WHERE id = 1")
    suspend fun updateDailyGoal(goal: Int)

//    @Query("UPDATE user_settings SET selected_character = :character WHERE id = 1")
//    suspend fun updateSelectedCharacter(character: String)

    @Query("UPDATE user_settings SET notifications_enabled = :enabled WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean)
}
