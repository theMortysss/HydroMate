package sdf.bitt.hydromate.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.data.local.entities.AchievementEntity

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements ORDER BY is_unlocked DESC, xp_reward DESC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): AchievementEntity?

    @Query("SELECT * FROM achievements WHERE is_unlocked = 1")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getAchievementsCount(): Int
}
