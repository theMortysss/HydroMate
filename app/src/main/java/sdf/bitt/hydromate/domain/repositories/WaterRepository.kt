package sdf.bitt.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.DrinkType
import sdf.bitt.hydromate.domain.entities.WaterEntry
import sdf.bitt.hydromate.domain.entities.WeeklyStatistics
import java.time.LocalDate

interface WaterRepository {

    suspend fun addWaterEntry(
        amount: Int,
        drink: Drink = Drink.WATER
    ): Result<Long>

    suspend fun deleteWaterEntry(entryId: Long): Result<Unit>

    fun getTodayProgress(): Flow<DailyProgress>

    fun getProgressForDate(date: LocalDate): Flow<DailyProgress>

    suspend fun getWeeklyStatistics(startDate: LocalDate): Result<WeeklyStatistics>

    suspend fun getLastEntry(): Result<WaterEntry?>

    fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WaterEntry>>
}