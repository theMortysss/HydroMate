package sdf.bitt.hydromate.domain.usecases

import com.google.firebase.Timestamp
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.DrinkType
import sdf.bitt.hydromate.domain.repositories.WaterRepository
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