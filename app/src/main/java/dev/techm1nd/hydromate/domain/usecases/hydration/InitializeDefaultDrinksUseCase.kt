package dev.techm1nd.hydromate.domain.usecases.hydration

import dev.techm1nd.hydromate.domain.repositories.DrinkRepository
import javax.inject.Inject

class InitializeDefaultDrinksUseCase @Inject constructor(
    private val drinkRepository: DrinkRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return drinkRepository.initializeDefaultDrinks()
    }
}