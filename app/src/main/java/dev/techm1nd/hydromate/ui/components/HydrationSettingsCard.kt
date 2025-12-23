package dev.techm1nd.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HydrationSettingsCard(
    showNetHydration: Boolean,
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
            // Display mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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
                    }
                }
                Switch(
                    checked = showNetHydration,
                    onCheckedChange = onShowNetHydrationToggle
                )
            }
        }
    }
}