package dev.techm1nd.hydromate.domain.usecases.hydration

import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.repositories.WaterRepository
import java.time.LocalDateTime
import javax.inject.Inject

class AddWaterEntryUseCase @Inject constructor(
    private val repository: WaterRepository
) {
    suspend operator fun invoke(
        amount: Int,
        drink: Drink = Drink.WATER,
        timestamp: LocalDateTime
    ): Result<Long> {
        return if (amount > 0) {
            repository.addWaterEntry(amount, drink, timestamp)
        } else {
            Result.failure(IllegalArgumentException("Amount must be positive"))
        }
    }
}