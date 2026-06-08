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
        title = "Уведомления",
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
                headlineContent = { Text("Включить напоминания") },
                supportingContent = { Text("Получайте уведомления о необходимости регулярно пить") },
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
                        headlineContent = { Text("Показывать индикатор выполнения") },
                        supportingContent = { Text("Отображать ход гидратации в уведомлениях") },
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
                        "Требуется разрешение на отправку уведомлений"
                    } else {
                        "Требуется разрешение на установку будильников"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = if (!hasNotificationPermission) {
                        "Нажмите, чтобы предоставить разрешение на отправку уведомлений"
                    } else {
                        "Нажмите, чтобы предоставить разрешение на установку будильников"
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
            text = "Часы работы",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Напоминания будут отправляться только в эти часы",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TimeCard(
                label = "Пробуждение",
                time = wakeUpTime.format(timeFormatter),
                icon = "🌅",
                onClick = onWakeUpTimeClick,
                modifier = Modifier.weight(1f)
            )

            TimeCard(
                label = "Время отхода ко сну",
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
                    text = "Умные напоминания",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Напоминания через интервал",
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
                    text = "Интервал напоминания",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                ReminderInterval.entries.forEach { interval ->
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
                    Text("Активные дни (${settings.smartReminderDays.size}/7)")
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
                    text = "Пользовательские напоминания",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Напоминания в определенное время суток",
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
                        text = "Пользовательских напоминаний пока нет",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = "${settings.customReminders.size} настроенных напоминаний",
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
                    Text("Управление пользовательскими напоминаниями")
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
                    text = "Отложенный режим",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Длительное нажатие на уведомление, чтобы отложить",
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
                    text = "Продолжительность повтора",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                SnoozeDelay.entries.forEach { delay ->
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
        title = { Text("Активные дни") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Выберите, в какие дни напоминания должны быть активны",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                DayOfWeek.entries.forEach { day ->
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
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
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
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
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
                            text = "Пользовательские напоминания",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tempReminders.size} напоминаний",
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
                            text = "Установите определенное время для напоминаний. Для каждого напоминания могут быть свои дни.",
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
                                text = "Пользовательских напоминаний пока нет",
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
                                    tempReminders[index] =
                                        reminder.copy(isEnabled = !reminder.isEnabled)
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
                            Text("Добавить напоминание")
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
                            Text("Отмена")
                        }

                        Button(
                            onClick = { onRemindersUpdated(tempReminders) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Сохранить")
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
                        text = "${reminder.enabledDays.size} дней активности",
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
                try {
                    DayOfWeek.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }?.toSet() ?: DayOfWeek.values().toSet()
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (reminder != null) "Редактировать напоминание" else "Добавить напоминание",
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
                                Text("Время", style = MaterialTheme.typography.labelMedium)
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
                    label = { Text("Название (опционально)") },
                    placeholder = { Text("н-р, Утреннее напоминание") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Days
                Text(
                    text = "Активные дни",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                DayOfWeek.entries.forEach { day ->
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
                        Text("Отмена")
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
                        Text("Сохранить")
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
