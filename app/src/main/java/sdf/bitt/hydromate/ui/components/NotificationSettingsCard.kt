package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun NotificationSettingsCard(
    notificationsEnabled: Boolean,
    notificationInterval: Int,
    wakeUpTime: LocalTime,
    bedTime: LocalTime,
    onNotificationsToggle: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onWakeUpTimeClick: () -> Unit,
    onBedTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    SettingsCard(
        title = "Notifications",
        icon = "🔔",
        modifier = modifier
    ) {
        // Enable notifications toggle
        ListItem(
            headlineContent = { Text("Enable Reminders") },
            supportingContent = { Text("Get notified to drink water regularly") },
            trailingContent = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsToggle
                )
            }
        )

        if (notificationsEnabled) {
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // Notification interval
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Reminder Interval",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "How often to remind you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val intervalOptions = listOf(30, 60, 90, 120)
                Column {
                    intervalOptions.forEach { interval ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = interval == notificationInterval,
                                    onClick = { onIntervalChange(interval) }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = interval == notificationInterval,
                                onClick = { onIntervalChange(interval) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (interval) {
                                    30 -> "Every 30 minutes"
                                    60 -> "Every hour"
                                    90 -> "Every 1.5 hours"
                                    120 -> "Every 2 hours"
                                    else -> "Every $interval minutes"
                                }
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // Wake up time
            ListItem(
                headlineContent = { Text("Wake Up Time") },
                supportingContent = { Text("When to start daily reminders") },
                trailingContent = {
                    Text(
                        text = wakeUpTime.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable { onWakeUpTimeClick() }
            )

            // Bed time
            ListItem(
                headlineContent = { Text("Bed Time") },
                supportingContent = { Text("When to stop daily reminders") },
                trailingContent = {
                    Text(
                        text = bedTime.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable { onBedTimeClick() }
            )
        }
    }
}
