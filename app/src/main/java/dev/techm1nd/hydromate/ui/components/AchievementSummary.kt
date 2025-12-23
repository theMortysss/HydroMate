package dev.techm1nd.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.techm1nd.hydromate.domain.entities.WeeklyStatistics

@Composable
fun AchievementSummary(
    weeklyStats: WeeklyStatistics,
    modifier: Modifier = Modifier
) {
    val achievements = generateAchievements(weeklyStats)

    if (achievements.isNotEmpty()) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Achievements This Week",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(achievements) { achievement ->
                        AchievementBadge(
                            achievement = achievement
                        )
                    }
                }

                if (achievements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keep going to unlock achievements! 💪",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    achievement: Achievement
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = achievement.icon,
                    fontSize = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = achievement.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2
        )
    }
}

private data class Achievement(
    val title: String,
    val icon: String,
    val description: String
)

private fun generateAchievements(weeklyStats: WeeklyStatistics): List<Achievement> {
    val achievements = mutableListOf<Achievement>()

    // Perfect week
    if (weeklyStats.daysGoalReached == 7) {
        achievements.add(
            Achievement(
                title = "Perfect Week",
                icon = "🏆",
                description = "Achieved daily goal every day"
            )
        )
    }

    // High streak
    if (weeklyStats.currentStreak >= 5) {
        achievements.add(
            Achievement(
                title = "On Fire",
                icon = "🔥",
                description = "${weeklyStats.currentStreak} day streak"
            )
        )
    }

    // High weekly intake
    if (weeklyStats.totalAmount >= 15000) {
        achievements.add(
            Achievement(
                title = "Hydration Hero",
                icon = "💧",
                description = "Excellent weekly intake"
            )
        )
    }

    // Consistency
    if (weeklyStats.daysGoalReached >= 5) {
        achievements.add(
            Achievement(
                title = "Consistent",
                icon = "📈",
                description = "Great consistency"
            )
        )
    }

    return achievements
}
