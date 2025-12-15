package sdf.bitt.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.Challenge
import sdf.bitt.hydromate.domain.entities.ChallengeType

interface ChallengeRepository {
    fun getActiveChallengesFlow(): Flow<List<Challenge>>
    suspend fun getActiveChallenges(): List<Challenge>
    suspend fun getActiveChallengeOfType(type: ChallengeType): Challenge?
    suspend fun getChallengeById(id: String): Challenge?
    suspend fun getAllChallenges(): List<Challenge>
    suspend fun insertChallenge(challenge: Challenge): Result<Challenge>
    suspend fun updateChallenge(challenge: Challenge): Result<Unit>
    suspend fun deleteChallenge(id: String): Result<Unit>
    fun getCompletedChallengesFlow(): Flow<List<Challenge>>
}
