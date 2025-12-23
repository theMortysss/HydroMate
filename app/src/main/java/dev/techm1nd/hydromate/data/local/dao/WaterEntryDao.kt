package dev.techm1nd.hydromate.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.data.local.entities.WaterEntryEntity

@Dao
interface WaterEntryDao {

    @Query("SELECT * FROM water_entries WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp ORDER BY timestamp DESC")
    fun getEntriesForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<WaterEntryEntity>>

    @Query("SELECT * FROM water_entries WHERE DATE(timestamp, 'unixepoch') = DATE(:timestamp, 'unixepoch') ORDER BY timestamp DESC")
    fun getEntriesForDay(timestamp: Long): Flow<List<WaterEntryEntity>>

    @Query("SELECT SUM(amount) FROM water_entries WHERE DATE(timestamp, 'unixepoch') = DATE(:timestamp, 'unixepoch')")
    suspend fun getTotalAmountForDay(timestamp: Long): Int?

    @Insert
    suspend fun insertEntry(entry: WaterEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entry: WaterEntryEntity)

    @Query("DELETE FROM water_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: Long)

    @Query("SELECT * FROM water_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEntry(): WaterEntryEntity?

    @Query("SELECT COUNT(*) FROM water_entries WHERE DATE(timestamp, 'unixepoch') = DATE(:timestamp, 'unixepoch')")
    suspend fun getEntryCountForDay(timestamp: Long): Int
}
