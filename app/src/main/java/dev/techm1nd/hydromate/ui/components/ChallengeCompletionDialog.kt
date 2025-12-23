package dev.techm1nd.hydromate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.techm1nd.hydromate.domain.usecases.challenge.CompleteChallengeUseCase
import kotlin.random.Random

@Composable
fun ChallengeCompletionDialog(
    result: CompleteChallengeUseCase.CompletionResult,
    onDismiss: () -> Unit
) {
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale.value),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box {
                // Confetti effect
                ConfettiEffect()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trophy icon
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text("🏆", fontSize = 56.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Challenge Complete!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = result.challenge.type.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Rewards
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Rewards",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("XP Gained:", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "+${result.xpGained} XP",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            result.achievementUnlocked?.let { achievement ->
                                Spacer(modifier = Modifier.height(8.dp))

                                Divider()

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "🎉 Achievement Unlocked!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(achievement.icon, fontSize = 32.sp)
                                    Column {
                                        Text(
                                            achievement.title,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            achievement.description,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                achievement.unlockableCharacter?.let { character ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "🎭 Character Unlocked!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(character.emoji, fontSize = 32.sp)
                                        Text(
                                            character.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome!")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfettiEffect() {
    var particles by remember {
        mutableStateOf(List(30) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.5f,
                color = listOf(
                    Color(0xFFFF6B6B),
                    Color(0xFF4ECDC4),
                    Color(0xFFFFE66D),
                    Color(0xFF95E1D3)
                ).random()
            )
        })
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            particles = particles.map { it.copy(y = it.y + 0.02f) }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color,
                radius = 8f,
                center = Offset(
                    x = particle.x * size.width,
                    y = (particle.y % 1f) * size.height
                )
            )
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val color: Color
)
