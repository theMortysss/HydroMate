package dev.techm1nd.hydromate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.techm1nd.hydromate.domain.entities.DailyProgress
import dev.techm1nd.hydromate.domain.entities.WaterEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun WaterCalendarEnhanced(
    month: YearMonth,
    monthlyProgress: Map<LocalDate, DailyProgress>,
    showNetHydration: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onAddWaterClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Water Intake Calendar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (showNetHydration) "Net" else "Total",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day of week headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar grid
            val firstDay = month.atDay(1)
            val lastDay = month.atEndOfMonth()
            val startOfCalendar = firstDay.minusDays((firstDay.dayOfWeek.value - 1).toLong())
            val endOfCalendar = lastDay.plusDays((7 - lastDay.dayOfWeek.value).toLong())

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(240.dp)
            ) {
                var currentDate = startOfCalendar
                while (!currentDate.isAfter(endOfCalendar)) {
                    val dateToAdd = currentDate
                    item {
                        CalendarDayEnhanced(
                            date = dateToAdd,
                            isCurrentMonth = dateToAdd.month == month.month,
                            progress = monthlyProgress[dateToAdd],
                            showNetHydration = showNetHydration,
                            onDateSelected = onDateSelected,
                            onAddWaterClick = onAddWaterClick
                        )
                    }
                    currentDate = currentDate.plusDays(1)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            CalendarLegend()
        }
    }
}

@Composable
fun CalendarDayEnhanced(
    date: LocalDate,
    isCurrentMonth: Boolean,
    progress: DailyProgress?,
    showNetHydration: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onAddWaterClick: (LocalDate) -> Unit
) {
    val isToday = date == LocalDate.now()
    val isPastOrToday = !date.isAfter(LocalDate.now())
    val hasData = progress != null && progress.totalAmount > 0

    val currentAmount = remember(progress, showNetHydration) {
        if (progress == null) 0
        else if (showNetHydration) progress.netHydration
        else progress.totalAmount
    }

    val isGoalReached = remember(progress, currentAmount) {
        progress != null && currentAmount >= progress.goalAmount
    }

    val backgroundColor = when {
        !isCurrentMonth -> Color.Transparent
        isGoalReached -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        hasData -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        isToday -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        isGoalReached -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = isCurrentMonth && (hasData || isPastOrToday)) {
                if (hasData) {
                    onDateSelected(date)
                } else if (isPastOrToday) {
                    onAddWaterClick(date)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 12.sp,
                color = textColor,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )

            // Индикатор для пустых дат текущего месяца
            if (isCurrentMonth && !hasData && isPastOrToday && !isToday) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add water",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }

            // Индикатор частичного прогресса
            if (hasData && !isGoalReached) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}


@Composable
fun DateDetailsModal(
    date: LocalDate,
    progress: DailyProgress,
    showNetHydration: Boolean,
    onDismiss: () -> Unit,
    onDeleteEntry: (Long) -> Unit = {},
    onAddMore: (LocalDate) -> Unit = {}
) {
    val isPastOrToday = !date.isAfter(LocalDate.now())

    val currentAmount = remember(progress, showNetHydration) {
        if (showNetHydration) progress.netHydration else progress.totalAmount
    }

    val progressPercentage = remember(currentAmount, progress.goalAmount) {
        (currentAmount.toFloat() / progress.goalAmount * 100).toInt()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
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
                            text = date.format(DateTimeFormatter.ofPattern("EEEE")),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display mode indicator
                if (showNetHydration) {
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
                        Text(
                            text = "📊 Showing net hydration values",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Progress overview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressMetric(
                        label = if (showNetHydration) "Net" else "Total",
                        value = "${currentAmount}ml",
                        icon = "💧"
                    )

                    ProgressMetric(
                        label = "Goal",
                        value = "${progress.goalAmount}ml",
                        icon = "🎯"
                    )

                    ProgressMetric(
                        label = "Progress",
                        value = "$progressPercentage%",
                        icon = if (currentAmount >= progress.goalAmount) "✅" else "⏳"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = (currentAmount.toFloat() / progress.goalAmount).coerceAtMost(1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(horizontal = 20.dp),
                    color = if (currentAmount >= progress.goalAmount) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Show hydration breakdown if using net hydration
                if (showNetHydration && progress.effectiveHydration != progress.totalAmount) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Hydration Breakdown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total consumed:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${progress.totalAmount}ml",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Effective hydration:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${progress.effectiveHydration}ml",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            val dehydration = progress.effectiveHydration - progress.netHydration
                            if (dehydration > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Dehydration effect:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        "-${dehydration}ml",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Entries list
                if (progress.entries.isNotEmpty()) {
                    Text(
                        text = "Water Entries (${progress.entries.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = progress.entries.sortedByDescending { it.timestamp },
                            key = { it.id }
                        ) { entry ->
                            EntryRowWithDelete(
                                entry = entry,
                                onDeleteClick = { onDeleteEntry(entry.id) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💧",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No entries for this day",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Achievement status
                if (currentAmount >= progress.goalAmount) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = "Daily Goal Achieved!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Great job staying hydrated!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                        alpha = 0.7f
                                    )
                                )
                            }
                        }
                    }
                }
                if (isPastOrToday) {
                    FloatingActionButton(
                        onClick = { onAddMore(date) },
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add more")
                            Text("Add More")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressMetric(
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
private fun EntryRowWithDelete(
    entry: WaterEntry,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.type.icon,
                    fontSize = 20.sp
                )
                Column {
                    Text(
                        text = "${entry.amount}ml",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = entry.type.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = entry.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete entry",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Entry") },
            text = {
                Text("Are you sure you want to delete this ${entry.amount}ml ${entry.type.displayName} entry?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MonthlySummaryEnhanced(
    monthlyProgress: List<DailyProgress>,
    showNetHydration: Boolean,
    modifier: Modifier = Modifier
) {
    if (monthlyProgress.isEmpty()) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No data for this month",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        return
    }

    val totalActual = monthlyProgress.sumOf { it.totalAmount }
    val totalDisplay = if (showNetHydration) {
        monthlyProgress.sumOf { it.netHydration }
    } else {
        totalActual
    }

    val averageDaily = totalDisplay / monthlyProgress.size
    val goalReachedDays = monthlyProgress.count {
        val amount = if (showNetHydration) it.netHydration else it.totalAmount
        amount >= it.goalAmount
    }
    val bestDay = monthlyProgress.maxByOrNull {
        if (showNetHydration) it.netHydration else it.totalAmount
    }
    val consistency = (goalReachedDays.toFloat() / monthlyProgress.size * 100).toInt()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
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
                    "Monthly Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (showNetHydration) "Net" else "Total",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Key metrics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric(
                    label = if (showNetHydration) "Net Total" else "Total",
                    value = "${totalDisplay / 1000f}L",
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

            // Best day highlight
            bestDay?.let { best ->
                Spacer(modifier = Modifier.height(20.dp))

                val bestDayAmount = if (showNetHydration) {
                    best.netHydration
                } else {
                    best.totalAmount
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.3f
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "⭐",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column {
                            Text(
                                "Best Day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "$bestDayAmount ml on ${
                                    best.date.format(
                                        DateTimeFormatter.ofPattern("MMM dd")
                                    )
                                }",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (bestDayAmount >= best.goalAmount) {
                                Text(
                                    "Goal achieved! 🏆",
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
        Text(icon, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            text = "Goal reached"
        )
        LegendItem(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            text = "Some progress"
        )
        LegendItem(
            color = Color.Transparent,
            text = "No data",
            showBorder = true
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String,
    showBorder: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
                .let { modifier ->
                    if (showBorder) {
                        modifier.background(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    } else modifier
                }
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}