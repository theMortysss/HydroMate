package sdf.bitt.hydromate.data.repositories

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import sdf.bitt.hydromate.data.local.dao.UserSettingsDao
import sdf.bitt.hydromate.data.local.dao.WaterEntryDao
import sdf.bitt.hydromate.data.mappers.UserSettingsMapper
import sdf.bitt.hydromate.data.mappers.WaterEntryMapper
import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.DrinkType
import sdf.bitt.hydromate.domain.entities.WaterEntry
import sdf.bitt.hydromate.domain.entities.WeeklyStatistics
import sdf.bitt.hydromate.domain.repositories.WaterRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterRepositoryImpl @Inject constructor(
    private val waterEntryDao: WaterEntryDao,
    private val userSettingsDao: UserSettingsDao
) : WaterRepository {

    override suspend fun addWaterEntry(amount: Int, drink: Drink): Result<Long> {
        return try {
            val entry = WaterEntry(
                amount = amount,
                timestamp = LocalDateTime.now(),
                drinkId = drink.id,
                type = drink.category
            )
            val id = waterEntryDao.insertEntry(WaterEntryMapper.toEntity(entry))
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteWaterEntry(entryId: Long): Result<Unit> {
        return try {
            waterEntryDao.deleteEntryById(entryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTodayProgress(): Flow<DailyProgress> {
        return getProgressForDate(LocalDate.now())
    }

    override fun getProgressForDate(date: LocalDate): Flow<DailyProgress> {
        val startOfDay = date.atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond()
        val endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

        return combine(
            waterEntryDao.getEntriesForDateRange(startOfDay, endOfDay),
            userSettingsDao.getUserSettings()
        ) { entities, settingsEntity ->
            val entries = WaterEntryMapper.toDomainList(entities)
            val settings = UserSettingsMapper.toDomain(settingsEntity)
            val totalAmount = entries.sumOf { it.amount }

            DailyProgress(
                date = date,
                totalAmount = totalAmount,
                goalAmount = settings.dailyGoal,
                entries = entries
            )
        }
    }

    override suspend fun getWeeklyStatistics(startDate: LocalDate): Result<WeeklyStatistics> {
        return try {
            val endDate = startDate.plusDays(6)
            val dailyProgressList = mutableListOf<DailyProgress>()

            for (i in 0..6) {
                val date = startDate.plusDays(i.toLong())
                val progress = getProgressForDate(date).first()
                dailyProgressList.add(progress)
            }
            Log.e("LIST", dailyProgressList.toString())
            val totalAmount = dailyProgressList.sumOf { it.totalAmount }
            val averageDaily = if (dailyProgressList.isNotEmpty()) totalAmount / dailyProgressList.size else 0
            val daysGoalReached = dailyProgressList.count { it.isGoalReached }
            val currentStreak = calculateStreak(dailyProgressList)

            val weeklyStats = WeeklyStatistics(
                weekStart = startDate,
                weekEnd = endDate,
                dailyProgress = dailyProgressList,
                totalAmount = totalAmount,
                averageDaily = averageDaily,
                daysGoalReached = daysGoalReached,
                currentStreak = currentStreak
            )

            Result.success(weeklyStats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastEntry(): Result<WaterEntry?> {
        return try {
            val entity = waterEntryDao.getLastEntry()
            val entry = entity?.let { WaterEntryMapper.toDomain(it) }
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WaterEntry>> {
        val startTimestamp = startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

        return waterEntryDao.getEntriesForDateRange(startTimestamp, endTimestamp)
            .map { WaterEntryMapper.toDomainList(it) }
    }

    private fun calculateStreak(dailyProgress: List<DailyProgress>): Int {
        var streak = 0
        for (progress in dailyProgress.reversed()) {
            if (progress.isGoalReached) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}
