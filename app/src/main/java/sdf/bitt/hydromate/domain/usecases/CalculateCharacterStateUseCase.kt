package sdf.bitt.hydromate.domain.usecases

import sdf.bitt.hydromate.domain.entities.DailyProgress
import javax.inject.Inject

class CalculateCharacterStateUseCase @Inject constructor() {

    operator fun invoke(progress: DailyProgress): CharacterState {
        return when {
            progress.progressPercentage >= 1.0f -> CharacterState.VERY_HAPPY
            progress.progressPercentage >= 0.75f -> CharacterState.HAPPY
            progress.progressPercentage >= 0.5f -> CharacterState.CONTENT
            progress.progressPercentage >= 0.25f -> CharacterState.SLIGHTLY_THIRSTY
            else -> CharacterState.THIRSTY
        }
    }

    enum class CharacterState {
        VERY_HAPPY,
        HAPPY,
        CONTENT,
        SLIGHTLY_THIRSTY,
        THIRSTY
    }
}