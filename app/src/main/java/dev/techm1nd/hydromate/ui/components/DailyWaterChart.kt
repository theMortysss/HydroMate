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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import java.time.format.DateTimeFormatter

@Composable
fun DailyWaterChart(
    dailyProgress: List<DailyProgress>,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, easing = EaseOutCubic),
        label = "chart_animation"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Daily Progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (dailyProgress.isEmpty()) {
                EmptyChartPlaceholder()
            } else {
                WaterChart(
                    dailyProgress = dailyProgress,
                    animationProgress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Day labels
                DayLabels(
                    dailyProgress = dailyProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WaterChart(
    dailyProgress: List<DailyProgress>,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val successColor = Color(0xFF4CAF50)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxAmount = dailyProgress.maxOfOrNull { it.goalAmount } ?: 2000
        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        val barWidth = chartWidth / dailyProgress.size.coerceAtLeast(1)

        dailyProgress.forEachIndexed { index, progress ->
            val barHeight = (progress.totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress
            val goalHeight = (progress.goalAmount.toFloat() / maxAmount) * chartHeight
            val x = padding + (index * barWidth) + barWidth * 0.1f
            val barActualWidth = barWidth * 0.8f

            // Goal line (dashed)
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(x, height - padding - goalHeight),
                end = Offset(x + barActualWidth, height - padding - goalHeight),
                strokeWidth = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(10f, 5f), 0f
                )
            )

            // Water bar with gradient
            val barColor = if (progress.isGoalReached) successColor else primaryColor
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    barColor.copy(alpha = 0.8f),
                    barColor.copy(alpha = 0.4f)
                ),
                startY = height - padding - barHeight,
                endY = height - padding
            )

            drawRect(
                brush = gradient,
                topLeft = Offset(x, height - padding - barHeight),
                size = Size(barActualWidth, barHeight)
            )

            // Bar border
            drawRect(
                color = barColor,
                topLeft = Offset(x, height - padding - barHeight),
                size = Size(barActualWidth, barHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun DayLabels(
    dailyProgress: List<DailyProgress>,
    modifier: Modifier = Modifier
) {
    val dayFormatter = DateTimeFormatter.ofPattern("E")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dailyProgress.forEach { progress ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = progress.date.format(dayFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${progress.totalAmount}ml",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyChartPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📊",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No data for this week",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
