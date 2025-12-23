package dev.techm1nd.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.entities.WaterEntry
import dev.techm1nd.hydromate.domain.entities.WeeklyStatistics
import java.time.LocalDate
import java.time.LocalDateTime

interface WaterRepository {

    suspend fun addWaterEntry(
        amount: Int,
        drink: Drink = Drink.WATER,
        timestamp: LocalDateTime
    ): Result<Long>

    suspend fun deleteWaterEntry(entryId: Long): Result<Unit>

    fun getTodayProgress(): Flow<DailyProgress>

    fun getProgressForDate(date: LocalDate): Flow<DailyProgress>

    fun getProgressForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyProgress>>

    suspend fun getWeeklyStatistics(startDate: LocalDate): Result<WeeklyStatistics>

    suspend fun getLastEntry(): Result<WaterEntry?>

    fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WaterEntry>>
}