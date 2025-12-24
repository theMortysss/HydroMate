package dev.techm1nd.hydromate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.entities.StatisticItem
import dev.techm1nd.hydromate.domain.entities.WeeklyStatistics
import dev.techm1nd.hydromate.domain.usecases.hydration.TotalHydration
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

@Composable
fun WeeklyOverviewCard(
    weeklyStats: WeeklyStatistics,
    hydrationData: TotalHydration?,
    modifier: Modifier = Modifier
) {
    val totalAmount = hydrationData?.netHydration ?: weeklyStats.totalAmount

    val averageDaily = totalAmount / 7

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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Weekly Overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewItem(
                    label = "Total",
                    value = "${totalAmount / 1000f}L",
                    icon = "💧"
                )

                OverviewItem(
                    label = "Daily Avg",
                    value = "${averageDaily}ml",
                    icon = "📊"
                )

                OverviewItem(
                    label = "Goals Hit",
                    value = "${weeklyStats.daysGoalReached}/7",
                    icon = "🎯"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            val weeklyGoal = weeklyStats.weeklyGoal
            val weekProgress = (totalAmount.toFloat() / weeklyGoal).coerceAtMost(1f)

            LinearProgressIndicator(
                progress = weekProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(weekProgress * 100).toInt()}% of weekly goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Показать дополнительную информацию если есть дегидратация
            if (hydrationData != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total consumed:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${hydrationData.totalActual}ml",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyWaterChartEnhanced(
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

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

            if (dailyProgress.all { it.totalAmount == 0 }) {
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
    val negativeColor = Color.Red

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        val barWidth = chartWidth / dailyProgress.size.coerceAtLeast(1)

        val minValue = dailyProgress.minOfOrNull {
            (it.netHydration).toFloat()
        } ?: 0f
        val maxValue = dailyProgress.maxOfOrNull {
            (it.netHydration).toFloat()
        } ?: 0f
        val maxGoal = dailyProgress.maxOfOrNull { it.goalAmount.toFloat() } ?: 2000f

        val effectiveMin = minOf(minValue, 0f)
        val effectiveMax = maxOf(maxValue, maxGoal, 0f)
        var range = effectiveMax - effectiveMin
        if (range == 0f) range = 2000f

        val yBottom = height - padding
        val yTop = padding

        fun yForValue(v: Float): Float {
            return yBottom - ((v - effectiveMin) / range) * chartHeight
        }

        dailyProgress.forEachIndexed { index, progress ->
            val currentAmount = progress.netHydration.toFloat()

            val animatedV = currentAmount * animationProgress
            val yZero = yForValue(0f)
            val yValueAnimated = yForValue(animatedV)
            val topY = min(yValueAnimated, yZero)
            val barHeight = max(yValueAnimated, yZero) - topY

            val goalY = yForValue(progress.goalAmount.toFloat())
            val x = padding + (index * barWidth) + barWidth * 0.1f
            val barActualWidth = barWidth * 0.8f

            // Goal line
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(x, goalY),
                end = Offset(x + barActualWidth, goalY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(10f, 5f), 0f
                )
            )

            // Bar
            val isGoalReached = currentAmount >= progress.goalAmount.toFloat()
            val barColor = when {
                isGoalReached -> successColor
                currentAmount < 0 -> negativeColor
                else -> primaryColor
            }

            val gradient = Brush.verticalGradient(
                colors = listOf(
                    barColor.copy(alpha = 0.8f),
                    barColor.copy(alpha = 0.4f)
                ),
                startY = topY,
                endY = topY + barHeight
            )

            drawRect(
                brush = gradient,
                topLeft = Offset(x, topY),
                size = Size(barActualWidth, barHeight)
            )

            drawRect(
                color = barColor,
                topLeft = Offset(x, topY),
                size = Size(barActualWidth, barHeight),
                style = Stroke(width = 2.dp.toPx())
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
            val amount = progress.netHydration

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
                    text = "${amount}ml",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun StatisticsCards(
    weeklyStats: WeeklyStatistics,
    hydrationData: TotalHydration?,
    modifier: Modifier = Modifier
) {
    val bestDayAmount = weeklyStats.dailyProgress.maxByOrNull {
        it.netHydration
    }?.netHydration ?: 0

    val totalAmount = hydrationData?.netHydration ?: weeklyStats.totalAmount

    val statisticsItems = listOf(
        StatisticItem(
            title = "Current Streak",
            value = "${weeklyStats.currentStreak} days",
            icon = "🔥",
            description = "Days in a row reaching goal"
        ),
        StatisticItem(
            title = "Best Day",
            value = "${bestDayAmount}ml",
            icon = "⭐",
            description = "Highest intake this week"
        ),
        StatisticItem(
            title = "Consistency",
            value = "${((weeklyStats.daysGoalReached.toFloat() / 7) * 100).toInt()}%",
            icon = "📈",
            description = "Goals achieved this week"
        ),
        StatisticItem(
            title = "Total Glasses",
            value = "${totalAmount / 250}",
            icon = "🥤",
            description = "Estimated glasses (250ml)"
        )
    )

    Column(modifier = modifier) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(statisticsItems) { item ->
                StatisticCard(
                    item = item,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
    }
}

@Composable
private fun OverviewItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun StatisticCard(
    item: StatisticItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.icon,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
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
