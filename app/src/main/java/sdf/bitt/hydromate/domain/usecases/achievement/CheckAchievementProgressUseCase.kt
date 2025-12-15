package sdf.bitt.hydromate.domain.usecases.achievement

import sdf.bitt.hydromate.domain.entities.Achievement
import sdf.bitt.hydromate.domain.entities.AchievementType
import sdf.bitt.hydromate.domain.repositories.AchievementRepository
import sdf.bitt.hydromate.domain.repositories.WaterRepository
import kotlinx.coroutines.flow.first
import sdf.bitt.hydromate.domain.entities.ChallengeType
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.repositories.ProfileRepository
import sdf.bitt.hydromate.domain.repositories.UserSettingsRepository
import sdf.bitt.hydromate.domain.usecases.challenge.GetCompletedChallengesUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.collections.count

class CheckAchievementProgressUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val waterRepository: WaterRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val profileRepository: ProfileRepository,
    private val unlockAchievementUseCase: UnlockAchievementUseCase,
    private val updateAchievementProgressUseCase: UpdateAchievementProgressUseCase,
    private val getCompletedChallengesUseCase: GetCompletedChallengesUseCase,
) {
    suspend operator fun invoke(): Result<List<Achievement>> {
        val newlyUnlocked = mutableListOf<Achievement>()

        // Проверяем все незаблокированные достижения
        val achievements = achievementRepository.getAllAchievements().first()
        val unlockedAchievements = achievements.filter { !it.isUnlocked }

        unlockedAchievements.forEach { achievement ->
            when (achievement.type) {
                AchievementType.STREAK_7, AchievementType.STREAK_30, AchievementType.STREAK_100 -> {
                    checkStreak(achievement)?.let { newlyUnlocked.add(it) }
                }
                AchievementType.PERFECT_WEEK,  AchievementType.PERFECT_MONTH -> {
                    checkPerfectPeriod(achievement)?.let { newlyUnlocked.add(it) }
                }
                AchievementType.TOTAL_1000ML, AchievementType.TOTAL_10000ML, AchievementType.TOTAL_100000ML -> {
                    checkTotalVolume(achievement)?.let { newlyUnlocked.add(it) }
                }
                AchievementType.EARLY_BIRD -> checkEarlyBird(achievement)
                AchievementType.NIGHT_OWL -> checkNightOwl(achievement)
                AchievementType.VARIETY_MASTER -> checkVarietyMaster(achievement)
                AchievementType.CHALLENGE_CAFFEINE_FREE_COMPLETED,
                AchievementType.CHALLENGE_ALCOHOL_FREE_COMPLETED,
                AchievementType.CHALLENGE_WATER_ONLY_COMPLETED,
                AchievementType.CHALLENGE_LACTOSE_FREE_COMPLETED,
                AchievementType.CHALLENGE_SUGAR_FREE_COMPLETED,
                AchievementType.CHALLENGE_SODA_FREE_COMPLETED,
                AchievementType.CHALLENGE_PLANT_BASED_COMPLETED,
                AchievementType.CHALLENGE_HYDRATION_HERO_COMPLETED -> checkChallengeCompletion(achievement)
                AchievementType.CHARACTER_UNLOCKED -> {}
            }
        }

        return Result.success(newlyUnlocked)
    }

    private suspend fun checkChallengeCompletion(achievement: Achievement): Achievement? {
        val completedChallenges = getCompletedChallengesUseCase().first()

        val requiredType = when (achievement.type) {
            AchievementType.CHALLENGE_CAFFEINE_FREE_COMPLETED -> ChallengeType.NO_CAFFEINE
            AchievementType.CHALLENGE_ALCOHOL_FREE_COMPLETED -> ChallengeType.NO_ALCOHOL
            AchievementType.CHALLENGE_WATER_ONLY_COMPLETED -> ChallengeType.WATER_ONLY
            AchievementType.CHALLENGE_LACTOSE_FREE_COMPLETED -> ChallengeType.NO_LACTOSE
            AchievementType.CHALLENGE_SUGAR_FREE_COMPLETED -> ChallengeType.NO_SUGAR
            AchievementType.CHALLENGE_SODA_FREE_COMPLETED -> ChallengeType.NO_SODA
            AchievementType.CHALLENGE_PLANT_BASED_COMPLETED -> ChallengeType.PLANT_BASED
            AchievementType.CHALLENGE_HYDRATION_HERO_COMPLETED -> ChallengeType.HYDRATION_HERO
            else -> return null
        }

        val hasCompleted = completedChallenges.any {
            it.type == requiredType && it.durationDays >= 14 && it.isCompleted && it.violations.isEmpty()
        }

        if (hasCompleted) {
            unlockAchievementUseCase(achievement.id)
            return achievement.copy(isUnlocked = true)
        }

        return null
    }

    private suspend fun checkStreak(achievement: Achievement): Achievement? {
        val today = LocalDate.now()
        val maxDays = achievement.progressMax
        val start = today.minusDays(maxDays.toLong() * 2) // Buffer for calculation

        val settings = userSettingsRepository.getUserSettings().first()
        val goal = settings.dailyGoal // Assume constant goal

        val entries = waterRepository.getEntriesForDateRange(start, today).first()

        val dailyTotals = entries.groupBy { it.timestamp.toLocalDate() }
            .mapValues { (_, dayEntries) -> dayEntries.sumOf { entry -> entry.amount } }

        var streak = 0
        for (i in 0 until maxDays + 1) {
            val date = today.minusDays(i.toLong())
            val total = dailyTotals[date] ?: 0
            if (total >= goal) {
                streak++
            } else {
                break
            }
        }

        updateAchievementProgressUseCase(achievement.id, streak)

        if (streak >= achievement.progressMax) {
            return unlockAchievementUseCase(achievement.id).getOrNull()
        }

        return null
    }

    private suspend fun checkPerfectPeriod(achievement: Achievement): Achievement? {
        val today = LocalDate.now()
        val settings = userSettingsRepository.getUserSettings().first()
        val goal = settings.dailyGoal // Assume constant goal for simplicity

        val isWeek = achievement.type == AchievementType.PERFECT_WEEK
        val periodDays = if (isWeek) 7 else 30 // Approximate month

        val startDate = if (isWeek) {
            today.minusDays(6) // Last 7 days including today
        } else {
            today.minusDays(29) // Last 30 days
        }

        val dailyProgress = waterRepository.getProgressForDateRange(startDate, today).first()

        val perfectDays = dailyProgress.count { it.totalAmount >= goal }

        val updatedAchievement = achievement.copy(progress = perfectDays)
        achievementRepository.updateAchievement(updatedAchievement)

        if (perfectDays >= periodDays) {
            unlockAchievementUseCase(achievement.id)
            return updatedAchievement.copy(isUnlocked = true)
        }

        return null
    }

    private suspend fun checkTotalVolume(achievement: Achievement): Achievement? {
        // Подсчитываем общий объем за все время
        val startDate = LocalDate.now().minusYears(1) // За последний год
        val endDate = LocalDate.now()

        val entries = waterRepository.getEntriesForDateRange(startDate, endDate).first()
        val totalVolume = entries.sumOf { it.amount }

        updateAchievementProgressUseCase(achievement.id, totalVolume)

        if (totalVolume >= achievement.progressMax) {
            return unlockAchievementUseCase(achievement.id).getOrNull()
        }

        return null
    }

    private suspend fun checkEarlyBird(achievement: Achievement): Achievement? {
        val settings = userSettingsRepository.getUserSettings().first()
        val startDate = LocalDate.now().minusYears(1) // Limit to last year for performance
        val entries = waterRepository.getEntriesForDateRange(startDate, LocalDate.now()).first()

        val earlyDaysCount = entries
            .groupBy { it.timestamp.toLocalDate() }
            .count { (date, dayEntries) ->
                val firstEntry = dayEntries.minByOrNull { it.timestamp } ?: return@count false
                val wakeUpDateTime = LocalDateTime.of(date, settings.wakeUpTime)
                firstEntry.timestamp.isAfter(wakeUpDateTime) &&
                        firstEntry.timestamp.isBefore(wakeUpDateTime.plusHours(1))
            }

        updateAchievementProgressUseCase(achievement.id, earlyDaysCount)

        if (earlyDaysCount >= achievement.progressMax) {
            return unlockAchievementUseCase(achievement.id).getOrNull()
        }

        return null
    }

    private suspend fun checkNightOwl(achievement: Achievement): Achievement? {
        val settings = userSettingsRepository.getUserSettings().first()
        val startDate = LocalDate.now().minusYears(1) // Limit to last year for performance
        val entries = waterRepository.getEntriesForDateRange(startDate, LocalDate.now()).first()

        val nightDaysCount = entries
            .groupBy { it.timestamp.toLocalDate() }
            .count { (date, dayEntries) ->
                val firstEntry = dayEntries.minByOrNull { it.timestamp } ?: return@count false
                val bedTimeDateTime = LocalDateTime.of(date, settings.bedTime)
                firstEntry.timestamp.isAfter(bedTimeDateTime.minusHours(1)) &&
                        firstEntry.timestamp.isBefore(bedTimeDateTime)
            }

        updateAchievementProgressUseCase(achievement.id, nightDaysCount)

        if (nightDaysCount >= achievement.progressMax) {
            return unlockAchievementUseCase(achievement.id).getOrNull()
        }

        return null
    }

    private suspend fun checkVarietyMaster(achievement: Achievement): Achievement? {
        val profile = profileRepository.getUserProfile().first()
        val variety = profile.uniqueDrinksTried.size
        updateAchievementProgressUseCase(achievement.id, variety)
        if (variety >= achievement.progressMax) {
            return unlockAchievementUseCase(achievement.id).getOrNull()
        }
        return null
    }
}
