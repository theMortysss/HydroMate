package sdf.bitt.hydromate.domain.usecases

import sdf.bitt.hydromate.domain.repositories.DrinkRepository
import javax.inject.Inject

class InitializeDefaultDrinksUseCase @Inject constructor(
    private val drinkRepository: DrinkRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return drinkRepository.initializeDefaultDrinks()
    }
}