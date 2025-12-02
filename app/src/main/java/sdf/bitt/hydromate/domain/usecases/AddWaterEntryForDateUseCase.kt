package sdf.bitt.hydromate.domain.usecases

import sdf.bitt.hydromate.data.local.dao.WaterEntryDao
import sdf.bitt.hydromate.data.mappers.WaterEntryMapper
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.WaterEntry
import sdf.bitt.hydromate.domain.repositories.WaterRepository
import java.time.LocalDateTime
import javax.inject.Inject

class AddWaterEntryForDateUseCase @Inject constructor(
    private val waterRepository: WaterRepository
) {
    suspend operator fun invoke(
        amount: Int,
        drink: Drink,
        time: LocalDateTime
    ): Result<Long> {
        return try {
            waterRepository.addWaterEntry(amount, drink, time)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}