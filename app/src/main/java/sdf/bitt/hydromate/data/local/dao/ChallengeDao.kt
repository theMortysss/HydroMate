package sdf.bitt.hydromate.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.data.local.entities.ChallengeEntity

@Dao
interface ChallengeDao {

    @Query("SELECT * FROM challenges WHERE is_active = 1 ORDER BY start_date DESC")
    fun getActiveChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE is_active = 1")
    suspend fun getActiveChallengesList(): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE is_active = 1 AND type = :type LIMIT 1")
    suspend fun getActiveChallengeOfType(type: String): ChallengeEntity?

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallengeById(id: String): ChallengeEntity?

    @Query("SELECT * FROM challenges ORDER BY start_date DESC")
    suspend fun getAllChallenges(): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE is_completed = 1 ORDER BY end_date DESC")
    fun getCompletedChallenges(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeEntity)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("DELETE FROM challenges WHERE id = :id")
    suspend fun deleteChallenge(id: String)
}
