package sdf.bitt.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow

interface TipsRepository {
    fun getViewedTipIds(): Flow<Set<String>>
    suspend fun markTipAsViewed(tipId: String): Result<Unit>
}