package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdf.bitt.hydromate.domain.entities.StatisticItem
import sdf.bitt.hydromate.domain.entities.WeeklyStatistics

@Composable
fun StatisticsCards(
    weeklyStats: WeeklyStatistics,
    modifier: Modifier = Modifier
) {
    val statisticsItems = listOf(
        StatisticItem(
            title = "Current Streak",
            value = "${weeklyStats.currentStreak} days",
            icon = "🔥",
            description = "Days in a row reaching goal"
        ),
        StatisticItem(
            title = "Best Day",
            value = "${weeklyStats.dailyProgress.maxByOrNull { it.totalAmount }?.totalAmount ?: 0}ml",
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
            value = "${weeklyStats.totalAmount / 250}",
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

