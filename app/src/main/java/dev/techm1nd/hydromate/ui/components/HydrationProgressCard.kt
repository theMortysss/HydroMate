package dev.techm1nd.hydromate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.techm1nd.hydromate.domain.entities.CharacterType
import dev.techm1nd.hydromate.domain.usecases.character.CalculateCharacterStateUseCase
import dev.techm1nd.hydromate.domain.usecases.hydration.HydrationProgress
import dev.techm1nd.hydromate.domain.usecases.hydration.TotalHydration
import dev.techm1nd.hydromate.ui.theme.HydroMateTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun HydrationProgressCard(
    hydrationProgress: HydrationProgress,
    totalHydration: TotalHydration,
    characterState: CalculateCharacterStateUseCase.CharacterState,
    selectedCharacter: CharacterType?,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = hydrationProgress.percentage / 100f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progress_animation"
    )
    val displayAmount = totalHydration.netHydration
    var showCharacter by remember { mutableStateOf(false) }  // Persistent state for visibility
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hydration Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                // Mode badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Total",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Progress with detailed info
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        coroutineScope.launch {
                            showCharacter = !showCharacter
                            delay(2000)
                            showCharacter = !showCharacter
                        }
                    }
            ) {
                if (!showCharacter) {
                    HydrationCircularProgress(
                        progress = animatedProgress,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${displayAmount}ml",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "of ${hydrationProgress.goal}ml",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${hydrationProgress.percentage.toInt()}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Show CharacterDisplay as overlay when toggled
                if (showCharacter) {
                    CharacterDisplay(
                        characterState = characterState,
                        selectedCharacter = selectedCharacter,
                        modifier = Modifier
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Detailed breakdown
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HydrationMetricRow(
                    icon = "💧",
                    label = "Total Consumed",
                    value = "${totalHydration.totalActual}ml",
                    color = Color(0xFF2196F3)
                )

                HydrationMetricRow(
                    icon = "✨",
                    label = "Effective Hydration",
                    value = "${totalHydration.totalEffective}ml",
                    color = Color(0xFF4CAF50)
                )
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                if (!hydrationProgress.isGoalReached) {
                    HydrationMetricRow(
                        icon = "📊",
                        label = "Remaining",
                        value = "${hydrationProgress.remaining}ml",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎉", fontSize = 24.sp)
                            Text(
                                text = "Daily hydration goal achieved!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HydrationCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_animation")

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) - 20.dp.toPx()

        // Background circle
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
        )

        // Progress arc with gradient
        if (progress > 0) {
            val sweepAngle = 360f * progress

            val gradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF2196F3),
                    Color(0xFF4CAF50),
                    Color(0xFF2196F3)
                ),
                center = Offset(centerX, centerY)
            )

            drawArc(
                brush = gradient,
                startAngle = -90f + sin(Math.toRadians(waveOffset.toDouble())).toFloat() * 5f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun HydrationMetricRow(
    icon: String,
    label: String,
    value: String,
    color: Color,
    isNegative: Boolean = false,
    isHighlighted: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                color.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isHighlighted) FontWeight.Medium else FontWeight.Normal
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isNegative) {
                    color.copy(alpha = 0.8f)
                } else {
                    color
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun HydrationProgressCard_Preview() {
    HydroMateTheme {
        HydrationProgressCard(
            hydrationProgress = HydrationProgress(),
            totalHydration = TotalHydration(),
            characterState = CalculateCharacterStateUseCase.CharacterState.HAPPY,
            selectedCharacter = CharacterType.PENGUIN,
            modifier = Modifier
        )
    }
}