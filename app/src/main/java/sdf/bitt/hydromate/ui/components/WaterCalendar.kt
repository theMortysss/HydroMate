package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdf.bitt.hydromate.domain.entities.DailyProgress
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun WaterCalendar(
    month: YearMonth,
    monthlyProgress: Map<LocalDate, DailyProgress>,
    onDateSelected: (LocalDate) -> Unit,
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
            Text(
                text = "Water Intake Calendar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                        CalendarDay(
                            date = dateToAdd,
                            isCurrentMonth = dateToAdd.month == month.month,
                            progress = monthlyProgress[dateToAdd],
                            onDateSelected = onDateSelected
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
private fun CalendarDay(
    date: LocalDate,
    isCurrentMonth: Boolean,
    progress: DailyProgress?,
    onDateSelected: (LocalDate) -> Unit
) {
    val isToday = date == LocalDate.now()
    val hasData = progress != null && progress.totalAmount > 0

    val backgroundColor = when {
        !isCurrentMonth -> Color.Transparent
        progress?.isGoalReached == true -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        hasData -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        isToday -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        progress?.isGoalReached == true -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = isCurrentMonth && hasData) {
                onDateSelected(date)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 12.sp,
            color = textColor,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )

        // Small indicator for partial progress
        if (hasData && progress?.isGoalReached != true) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-2).dp)
            )
        }
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
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
