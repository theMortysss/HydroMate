package dev.techm1nd.hydromate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.techm1nd.hydromate.ui.theme.HydroMateTheme
import kotlin.math.sin

@Composable
fun ProgressCard(
    currentAmount: Int,
    goalAmount: Int,
    progressPercentage: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progress_animation"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                CircularWaveProgress(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentAmount}ml",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "of ${goalAmount}ml",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Remaining amount
            if (progressPercentage < 1f) {
                Text(
                    text = "${goalAmount - currentAmount}ml remaining",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = "Daily goal completed! 🎉",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CircularWaveProgress(
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
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Progress arc
        if (progress > 0) {
            drawProgressArc(
                progress = progress,
                centerX = centerX,
                centerY = centerY,
                radius = radius,
                waveOffset = waveOffset
            )
        }
    }
}

private fun DrawScope.drawProgressArc(
    progress: Float,
    centerX: Float,
    centerY: Float,
    radius: Float,
    waveOffset: Float
) {
    val sweepAngle = 360f * progress

    val gradient = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF2196F3),
            Color(0xFF21CBF3),
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
