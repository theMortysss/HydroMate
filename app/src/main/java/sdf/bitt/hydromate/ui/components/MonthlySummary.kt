package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdf.bitt.hydromate.domain.entities.DailyProgress

@Composable
fun MonthlySummary(
    monthlyProgress: List<DailyProgress>,
    modifier: Modifier = Modifier
) {
    if (monthlyProgress.isEmpty()) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
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
                        text = "No data for this month",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Start tracking your water intake!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
        return
    }

    val totalAmount = monthlyProgress.sumOf { it.totalAmount }
    val averageDaily = totalAmount / monthlyProgress.size
    val goalReachedDays = monthlyProgress.count { it.isGoalReached }
    val bestDay = monthlyProgress.maxByOrNull { it.totalAmount }
    val consistency = (goalReachedDays.toFloat() / monthlyProgress.size * 100).toInt()

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
                text = "Monthly Summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Key metrics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric(
                    label = "Total",
                    value = "${totalAmount / 1000f}L",
                    subtitle = "${monthlyProgress.size} days tracked",
                    icon = "💧",
                    modifier = Modifier.weight(1f)
                )

                SummaryMetric(
                    label = "Daily Avg",
                    value = "${averageDaily}ml",
                    subtitle = "per day",
                    icon = "📊",
                    modifier = Modifier.weight(1f)
                )

                SummaryMetric(
                    label = "Goals Hit",
                    value = "$goalReachedDays",
                    subtitle = "$consistency% success",
                    icon = "🎯",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Best day highlight
            bestDay?.let { best ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column {
                            Text(
                                text = "Best Day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${best.totalAmount}ml on ${best.date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (best.isGoalReached) {
                                Text(
                                    text = "Goal achieved! 🏆",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
