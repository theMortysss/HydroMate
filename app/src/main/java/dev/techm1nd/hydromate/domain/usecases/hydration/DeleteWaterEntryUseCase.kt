package dev.techm1nd.hydromate.domain.usecases.hydration

import dev.techm1nd.hydromate.domain.repositories.WaterRepository
import javax.inject.Inject

class DeleteWaterEntryUseCase @Inject constructor(
    private val repository: WaterRepository
) {
    suspend operator fun invoke(entryId: Long): Result<Unit> {
        return repository.deleteWaterEntry(entryId)
    }
}