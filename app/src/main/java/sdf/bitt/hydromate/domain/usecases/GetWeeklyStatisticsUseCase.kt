package sdf.bitt.hydromate.domain.usecases

import sdf.bitt.hydromate.domain.entities.WeeklyStatistics
import sdf.bitt.hydromate.domain.repositories.WaterRepository
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklyStatisticsUseCase @Inject constructor(
    private val repository: WaterRepository
) {
    suspend operator fun invoke(startDate: LocalDate = getWeekStart()): Result<WeeklyStatistics> {
        return repository.getWeeklyStatistics(startDate)
    }

    private fun getWeekStart(): LocalDate {
        val now = LocalDate.now()
        val dayOfWeek = now.dayOfWeek.value
        return now.minusDays((dayOfWeek - 1).toLong())
    }
}