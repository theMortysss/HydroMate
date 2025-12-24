package dev.techm1nd.hydromate.domain.usecases.stat

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.repositories.WaterRepository
import javax.inject.Inject

class GetTodayProgressUseCase @Inject constructor(
    private val repository: WaterRepository
) {
    operator fun invoke(): Flow<DailyProgress> {
        return repository.getTodayProgress()
    }
}