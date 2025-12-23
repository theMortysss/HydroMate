package dev.techm1nd.hydromate.domain.usecases

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.repositories.WaterRepository
import java.time.LocalDate
import javax.inject.Inject

class GetProgressForDateUseCase @Inject constructor(
    private val repository: WaterRepository
) {
    operator fun invoke(date: LocalDate): Flow<DailyProgress> {
        return repository.getProgressForDate(date)
    }
}