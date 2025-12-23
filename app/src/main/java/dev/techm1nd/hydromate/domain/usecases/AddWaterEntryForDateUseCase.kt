package dev.techm1nd.hydromate.domain.usecases

import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.repositories.WaterRepository
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