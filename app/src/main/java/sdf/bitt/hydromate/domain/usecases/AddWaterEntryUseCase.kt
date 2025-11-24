package sdf.bitt.hydromate.domain.usecases

import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.DrinkType
import sdf.bitt.hydromate.domain.repositories.WaterRepository
import javax.inject.Inject

class AddWaterEntryUseCase @Inject constructor(
    private val repository: WaterRepository
) {
    suspend operator fun invoke(
        amount: Int,
        drink: Drink = Drink.WATER
    ): Result<Long> {
        return if (amount > 0) {
            repository.addWaterEntry(amount, drink)
        } else {
            Result.failure(IllegalArgumentException("Amount must be positive"))
        }
    }
}