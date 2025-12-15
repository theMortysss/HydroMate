package sdf.bitt.hydromate.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sdf.bitt.hydromate.data.local.dao.ChallengeDao
import sdf.bitt.hydromate.data.mappers.ChallengeMapper
import sdf.bitt.hydromate.domain.entities.Challenge
import sdf.bitt.hydromate.domain.entities.ChallengeType
import sdf.bitt.hydromate.domain.repositories.ChallengeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val challengeDao: ChallengeDao
) : ChallengeRepository {

    override fun getActiveChallengesFlow(): Flow<List<Challenge>> {
        return challengeDao.getActiveChallenges()
            .map { entities -> entities.map { ChallengeMapper.toDomain(it) } }
    }

    override suspend fun getActiveChallenges(): List<Challenge> {
        return challengeDao.getActiveChallengesList()
            .map { ChallengeMapper.toDomain(it) }
    }

    override suspend fun getActiveChallengeOfType(type: ChallengeType): Challenge? {
        return challengeDao.getActiveChallengeOfType(type.name)
            ?.let { ChallengeMapper.toDomain(it) }
    }

    override suspend fun getChallengeById(id: String): Challenge? {
        return challengeDao.getChallengeById(id)
            ?.let { ChallengeMapper.toDomain(it) }
    }

    override suspend fun getAllChallenges(): List<Challenge> {
        return challengeDao.getAllChallenges()
            .map { ChallengeMapper.toDomain(it) }
    }

    override suspend fun insertChallenge(challenge: Challenge): Result<Challenge> {
        return try {
            val entity = ChallengeMapper.toEntity(challenge)
            challengeDao.insertChallenge(entity)
            Result.success(challenge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChallenge(challenge: Challenge): Result<Unit> {
        return try {
            val entity = ChallengeMapper.toEntity(challenge)
            challengeDao.updateChallenge(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteChallenge(id: String): Result<Unit> {
        return try {
            challengeDao.deleteChallenge(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCompletedChallengesFlow(): Flow<List<Challenge>> {
        return challengeDao.getCompletedChallenges()
            .map { entities -> entities.map { ChallengeMapper.toDomain(it) } }
    }
}
