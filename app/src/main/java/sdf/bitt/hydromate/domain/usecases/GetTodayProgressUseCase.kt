package sdf.bitt.hydromate.domain.usecases

import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.domain.entities.DailyProgress
import sdf.bitt.hydromate.domain.repositories.WaterRepository
import javax.inject.Inject

class GetTodayProgressUseCase @Inject constructor(
    private val repository: WaterRepository
) {
    operator fun invoke(): Flow<DailyProgress> {
        return repository.getTodayProgress()
    }
}