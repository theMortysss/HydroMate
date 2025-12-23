package dev.techm1nd.hydromate.ui.components

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.techm1nd.hydromate.domain.entities.*
import dev.techm1nd.hydromate.ui.notification.NotificationPermissionHelper
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun NotificationSettingsCard(
    settings: UserSettings,
    onSettingsUpdate: (UserSettings) -> Unit,
    onWakeUpTimeClick: () -> Unit,
    onBedTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val hasNotificationPermission = remember {
        NotificationPermissionHelper.hasNotificationPermission(context)
    }

    val hasExactAlarmPermission = remember {
        NotificationPermissionHelper.hasExactAlarmPermission(context)
    }

    var showDaysDialog by remember { mutableStateOf(false) }
    var showCustomRemindersDialog by remember { mutableStateOf(false) }

    SettingsCard(
        title = "Notifications",
        icon = "🔔",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Permission warning
            if (!hasNotificationPermission || !hasExactAlarmPermission) {
                PermissionWarningCard(
                    hasNotificationPermission = hasNotificationPermission,
                    hasExactAlarmPermission = hasExactAlarmPermission,
                    onRequestPermission = {
                        if (!hasNotificationPermission) {
                            activity?.let {
                                NotificationPermissionHelper.requestNotificationPermission(it)
                            }
                        } else {
                            NotificationPermissionHelper.openExactAlarmSettings(context)
                        }
                    }
                )
            }

            // Master toggle
            ListItem(
                headlineContent = { Text("Enable Reminders") },
                supportingContent = { Text("Get notified to drink water regularly") },
                trailingContent = {
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            onSettingsUpdate(settings.copy(notificationsEnabled = enabled))
                        },
                        enabled = hasNotificationPermission && hasExactAlarmPermission
                    )
                }
            )

            AnimatedVisibility(
                visible = settings.notificationsEnabled &&
                        hasNotificationPermission &&
                        hasExactAlarmPermission
            ) {
                Column {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Active hours
                    ActiveHoursSection(
                        wakeUpTime = settings.wakeUpTime,
                        bedTime = settings.bedTime,
                        onWakeUpTimeClick = onWakeUpTimeClick,
                        onBedTimeClick = onBedTimeClick,
                        timeFormatter = timeFormatter
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Smart reminders
                    SmartRemindersSection(
                        settings = settings,
                        onSettingsUpdate = onSettingsUpdate,
                        onShowDaysDialog = { showDaysDialog = true }
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Custom reminders
                    CustomRemindersSection(
                        settings = settings,
                        onSettingsUpdate = onSettingsUpdate,
                        onShowRemindersDialog = { showCustomRemindersDialog = true }
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Snooze settings
                    SnoozeSettingsSection(
                        settings = settings,
                        onSettingsUpdate = onSettingsUpdate
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Progress in notification
                    ListItem(
                        headlineContent = { Text("Show Progress Bar") },
                        supportingContent = { Text("Display hydration progress in notifications") },
                        trailingContent = {
                            Switch(
                                checked = settings.showProgressInNotification,
                                onCheckedChange = { show ->
                                    onSettingsUpdate(
                                        settings.copy(showProgressInNotification = show)
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showDaysDialog) {
        DaysSelectionDialog(
            selectedDays = settings.smartReminderDays,
            onDaysSelected = { days ->
                onSettingsUpdate(settings.copy(smartReminderDays = days))
                showDaysDialog = false
            },
            onDismiss = { showDaysDialog = false }
        )
    }

    if (showCustomRemindersDialog) {
        CustomRemindersDialog(
            reminders = settings.customReminders,
            onRemindersUpdated = { reminders ->
                onSettingsUpdate(settings.copy(customReminders = reminders))
                showCustomRemindersDialog = false
            },
            onDismiss = { showCustomRemindersDialog = false }
        )
    }
}

@Composable
private fun PermissionWarningCard(
    hasNotificationPermission: Boolean,
    hasExactAlarmPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onRequestPermission() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (!hasNotificationPermission) {
                        "Notification Permission Required"
                    } else {
                        "Exact Alarm Permission Required"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = if (!hasNotificationPermission) {
                        "Tap to grant notification permission"
                    } else {
                        "Tap to enable exact alarms for precise reminders"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ActiveHoursSection(
    wakeUpTime: LocalTime,
    bedTime: LocalTime,
    onWakeUpTimeClick: () -> Unit,
    onBedTimeClick: () -> Unit,
    timeFormatter: DateTimeFormatter
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Active Hours",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Reminders will only be sent during these hours",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TimeCard(
                label = "Wake Up",
                time = wakeUpTime.format(timeFormatter),
                icon = "🌅",
                onClick = onWakeUpTimeClick,
                modifier = Modifier.weight(1f)
            )

            TimeCard(
                label = "Bed Time",
                time = bedTime.format(timeFormatter),
                icon = "🌙",
                onClick = onBedTimeClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimeCard(
    label: String,
    time: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SmartRemindersSection(
    settings: UserSettings,
    onSettingsUpdate: (UserSettings) -> Unit,
    onShowDaysDialog: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Automatic reminders at regular intervals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Switch(
                checked = settings.smartRemindersEnabled,
                onCheckedChange = { enabled ->
                    onSettingsUpdate(settings.copy(smartRemindersEnabled = enabled))
                }
            )
        }

        AnimatedVisibility(visible = settings.smartRemindersEnabled) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Reminder Interval",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                ReminderInterval.values().forEach { interval ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = interval == settings.reminderInterval,
                                onClick = {
                                    onSettingsUpdate(
                                        settings.copy(
                                            reminderInterval = interval,
                                            notificationInterval = interval.minutes
                                        )
                                    )
                                }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = interval == settings.reminderInterval,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = interval.displayName)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onShowDaysDialog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Active Days (${settings.smartReminderDays.size}/7)")
                }
            }
        }
    }
}

@Composable
private fun CustomRemindersSection(
    settings: UserSettings,
    onSettingsUpdate: (UserSettings) -> Unit,
    onShowRemindersDialog: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Custom Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Reminders at specific times of day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Switch(
                checked = settings.customRemindersEnabled,
                onCheckedChange = { enabled ->
                    onSettingsUpdate(settings.copy(customRemindersEnabled = enabled))
                }
            )
        }

        AnimatedVisibility(visible = settings.customRemindersEnabled) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                if (settings.customReminders.isEmpty()) {
                    Text(
                        text = "No custom reminders yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = "${settings.customReminders.size} reminder(s) configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onShowRemindersDialog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Custom Reminders")
                }
            }
        }
    }
}

@Composable
private fun SnoozeSettingsSection(
    settings: UserSettings,
    onSettingsUpdate: (UserSettings) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Snooze on Hold",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Long-press notification to snooze",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Switch(
                checked = settings.snoozeEnabled,
                onCheckedChange = { enabled ->
                    onSettingsUpdate(settings.copy(snoozeEnabled = enabled))
                }
            )
        }

        AnimatedVisibility(visible = settings.snoozeEnabled) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Snooze Duration",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                SnoozeDelay.values().forEach { delay ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = delay == settings.snoozeDelay,
                                onClick = {
                                    onSettingsUpdate(settings.copy(snoozeDelay = delay))
                                }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = delay == settings.snoozeDelay,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = delay.displayName)
                    }
                }
            }
        }
    }
}

@Composable
fun DaysSelectionDialog(
    selectedDays: Set<DayOfWeek>,
    onDaysSelected: (Set<DayOfWeek>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedDays by remember { mutableStateOf(selectedDays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("📅", fontSize = 32.sp) },
        title = { Text("Select Active Days") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Choose which days reminders should be active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                DayOfWeek.values().forEach { day ->
                    DayCheckboxItem(
                        day = day,
                        isSelected = tempSelectedDays.contains(day),
                        onCheckedChange = { checked ->
                            tempSelectedDays = if (checked) {
                                tempSelectedDays + day
                            } else {
                                tempSelectedDays - day
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tempSelectedDays.isNotEmpty()) {
                        onDaysSelected(tempSelectedDays)
                    }
                },
                enabled = tempSelectedDays.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DayCheckboxItem(
    day: DayOfWeek,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isSelected) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = day.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CustomRemindersDialog(
    reminders: List<CustomReminder>,
    onRemindersUpdated: (List<CustomReminder>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempReminders by remember { mutableStateOf(reminders.toMutableList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<CustomReminder?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Custom Reminders",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tempReminders.size} reminder(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                            alpha = 0.5f
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💡", fontSize = 20.sp)
                        Text(
                            text = "Set specific times for reminders. Each reminder can have its own active days.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminders list
                if (tempReminders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⏰", fontSize = 48.sp)
                            Text(
                                text = "No custom reminders yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(tempReminders) { index, reminder ->
                            CustomReminderItem(
                                reminder = reminder,
                                onEdit = { editingReminder = reminder },
                                onDelete = { tempReminders.removeAt(index) },
                                onToggleEnabled = {
                                    tempReminders[index] = reminder.copy(isEnabled = !reminder.isEnabled)
                                }
                            )
                        }
                    }
                }

                // Actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (tempReminders.size < 10) {
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Reminder")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { onRemindersUpdated(tempReminders) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingReminder != null) {
        AddEditReminderDialog(
            reminder = editingReminder,
            onSave = { newReminder ->
                if (editingReminder != null) {
                    val index = tempReminders.indexOfFirst { it.id == editingReminder!!.id }
                    if (index != -1) {
                        tempReminders[index] = newReminder
                    }
                } else {
                    tempReminders.add(newReminder)
                }
                showAddDialog = false
                editingReminder = null
            },
            onDismiss = {
                showAddDialog = false
                editingReminder = null
            }
        )
    }
}

@Composable
private fun CustomReminderItem(
    reminder: CustomReminder,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = if (reminder.isEnabled) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text("⏰", fontSize = 28.sp)
                Column {
                    Text(
                        text = reminder.time,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (reminder.label.isNotBlank()) {
                        Text(
                            text = reminder.label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "${reminder.enabledDays.size} days active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggleEnabled() }
                )

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AddEditReminderDialog(
    reminder: CustomReminder?,
    onSave: (CustomReminder) -> Unit,
    onDismiss: () -> Unit
) {
    var time by remember { mutableStateOf(reminder?.time ?: "09:00") }
    var label by remember { mutableStateOf(reminder?.label ?: "") }
    var enabledDays by remember {
        mutableStateOf(
            reminder?.enabledDays?.mapNotNull {
                try { DayOfWeek.valueOf(it) } catch (e: Exception) { null }
            }?.toSet() ?: DayOfWeek.values().toSet()
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (reminder != null) "Edit Reminder" else "Add Reminder",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Time
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("⏰", fontSize = 28.sp)
                            Column {
                                Text("Time", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(Icons.Default.Edit, contentDescription = "Edit time")
                    }
                }

                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    placeholder = { Text("e.g., Morning hydration") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Days
                Text(
                    text = "Active Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                DayOfWeek.values().forEach { day ->
                    DayCheckboxItem(
                        day = day,
                        isSelected = enabledDays.contains(day),
                        onCheckedChange = { checked ->
                            enabledDays = if (checked) {
                                enabledDays + day
                            } else {
                                enabledDays - day
                            }
                        }
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (enabledDays.isNotEmpty()) {
                                val newReminder = CustomReminder(
                                    id = reminder?.id ?: java.util.UUID.randomUUID().toString(),
                                    time = time,
                                    label = label,
                                    enabledDays = enabledDays.map { it.name }.toSet(),
                                    isEnabled = reminder?.isEnabled ?: true
                                )
                                onSave(newReminder)
                            }
                        },
                        enabled = enabledDays.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val parts = time.split(":")
        TimePickerDialog(
            initialHour = parts[0].toIntOrNull() ?: 9,
            initialMinute = parts[1].toIntOrNull() ?: 0,
            onTimeSelected = { hour, minute ->
                time = String.format("%02d:%02d", hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
