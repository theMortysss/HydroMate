package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun HydrationSettingsCard(
    hydrationThreshold: Float,
    showNetHydration: Boolean,
    onThresholdChange: (Float) -> Unit,
    onShowNetHydrationToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        title = "Hydration Calculation",
        icon = "⚗️",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
//            // Display mode toggle
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
                Column {
                    Text(
                        text = "Show Net Hydration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (showNetHydration) {
                            "Displays effective hydration after adjustments"
                        } else {
                            "Displays total liquid consumed"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Switch(
                        checked = showNetHydration,
                        onCheckedChange = onShowNetHydrationToggle
                    )
                }

                Divider()

                // Hydration threshold slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hydration Goal Threshold",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(hydrationThreshold * 100).roundToInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Adjust how strict your daily goal is",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = hydrationThreshold,
                        onValueChange = onThresholdChange,
                        valueRange = 0.8f..1.2f,
                        steps = 7, // 80%, 85%, 90%, 95%, 100%, 105%, 110%, 115%, 120%
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description based on threshold
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                hydrationThreshold < 0.9f -> MaterialTheme.colorScheme.errorContainer
                                hydrationThreshold < 1.0f -> MaterialTheme.colorScheme.tertiaryContainer
                                hydrationThreshold > 1.1f -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when {
                                    hydrationThreshold < 0.9f -> "🔥"
                                    hydrationThreshold < 1.0f -> "💪"
                                    hydrationThreshold > 1.1f -> "🌟"
                                    else -> "✅"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Column {
                                Text(
                                    text = when {
                                        hydrationThreshold < 0.9f -> "Challenging Mode"
                                        hydrationThreshold < 1.0f -> "Active Mode"
                                        hydrationThreshold > 1.1f -> "Ambitious Mode"
                                        else -> "Standard Mode"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when {
                                        hydrationThreshold < 0.9f -> "Easier to reach your goal. Good for building habits!"
                                        hydrationThreshold < 1.0f -> "Slightly easier goals for active days"
                                        hydrationThreshold > 1.1f -> "Higher goals for peak hydration"
                                        else -> "Standard recommendations for most people"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Divider()

                // Info card about hydration calculation
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ℹ️", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "How Hydration is Calculated",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "• Different drinks hydrate differently (tea ~95%, soda ~70%)\n" +
                                    "• Caffeine causes ~5% dehydration\n" +
                                    "• Alcohol causes ~15% dehydration\n" +
                                    "• Net hydration = effective amount - dehydration",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
//            }
        }
    }
}