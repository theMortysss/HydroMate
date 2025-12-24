package dev.techm1nd.hydromate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.techm1nd.hydromate.domain.entities.CharacterType
import dev.techm1nd.hydromate.domain.usecases.character.CalculateCharacterStateUseCase

@Composable
fun CharacterDisplay(
    characterState: CalculateCharacterStateUseCase.CharacterState,
    selectedCharacter: CharacterType?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "character_animation")

    // Bounce animation based on character state
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = when (characterState) {
            CalculateCharacterStateUseCase.CharacterState.VERY_HAPPY -> 1.1f
            CalculateCharacterStateUseCase.CharacterState.HAPPY -> 1.05f
            else -> 1f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_scale"
    )

    // Color based on state
    val characterColor = when (characterState) {
        CalculateCharacterStateUseCase.CharacterState.VERY_HAPPY -> Color(0xFF4CAF50)
        CalculateCharacterStateUseCase.CharacterState.HAPPY -> Color(0xFF8BC34A)
        CalculateCharacterStateUseCase.CharacterState.CONTENT -> Color(0xFF2196F3)
        CalculateCharacterStateUseCase.CharacterState.SLIGHTLY_THIRSTY -> Color(0xFFFF9800)
        CalculateCharacterStateUseCase.CharacterState.THIRSTY -> Color(0xFFE57373)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = characterColor.copy(alpha = 0.1f),
            modifier = Modifier
                .size(160.dp)
                .scale(bounceScale)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = selectedCharacter?.emoji ?: "🐧",
                    fontSize = 80.sp,
                    modifier = Modifier
                )

                // Water drops effect for very happy state
                if (characterState == CalculateCharacterStateUseCase.CharacterState.VERY_HAPPY) {
                    WaterDropsEffect(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = getCharacterMessage(characterState),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WaterDropsEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "water_drops")

    val drop1Y by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drop1_y"
    )

    val drop2Y by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drop2_y"
    )

    Canvas(modifier = modifier) {
        drawWaterDrop(30f, drop1Y, size.width * 0.3f)
        drawWaterDrop(25f, drop2Y, size.width * 0.7f)
    }
}

private fun DrawScope.drawWaterDrop(radius: Float, y: Float, x: Float) {
    if (y > -radius && y < size.height) {
        drawCircle(
            color = Color(0xFF2196F3).copy(alpha = 0.6f),
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }
}

private fun getCharacterMessage(state: CalculateCharacterStateUseCase.CharacterState): String {
    return when (state) {
        CalculateCharacterStateUseCase.CharacterState.VERY_HAPPY -> "Amazing! Goal completed! 🎉"
        CalculateCharacterStateUseCase.CharacterState.HAPPY -> "Doing great! Keep it up! 😊"
        CalculateCharacterStateUseCase.CharacterState.CONTENT -> "Good progress! 👍"
        CalculateCharacterStateUseCase.CharacterState.SLIGHTLY_THIRSTY -> "Time for some water! 💧"
        CalculateCharacterStateUseCase.CharacterState.THIRSTY -> "I'm thirsty! Help me! 🥺"
    }
}
