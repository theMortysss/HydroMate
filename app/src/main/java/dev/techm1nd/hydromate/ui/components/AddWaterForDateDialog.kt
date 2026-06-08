package dev.techm1nd.hydromate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import dev.techm1nd.hydromate.domain.entities.Drink
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import dev.techm1nd.hydromate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWaterForDateDialog(
    date: LocalDate,
    drinks: List<Drink>,
    onAddEntry: (amount: Int, drink: Drink, time: LocalDateTime) -> Unit,
    onDrinkCreated: (Drink) -> Unit,
    onDismiss: () -> Unit
) {
    val now = LocalDateTime.now()
    var amount by remember { mutableStateOf("") }
    var selectedDrink by remember {
        mutableStateOf(drinks.firstOrNull { it.id == 1L } ?: Drink.WATER)
    }
    var selectedHour by remember { mutableStateOf(now.hour) }
    var selectedMinute by remember { mutableStateOf(now.minute) }
    var showDrinkSelector by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCreateDrink by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ═══════════════════════════════════════════════════════════════
                // HEADER: Заголовок с датой
                // ═══════════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.add_water_for_date),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = date.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // ═══════════════════════════════════════════════════════════════
                // FORM: Форма ввода данных
                // ═══════════════════════════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Информационная карточка
                    Card(
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 24.sp)
                            Text(
                                text = stringResource(R.string.add_water_info),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    // Селектор напитка
                    Text(
                        text = stringResource(R.string.select_drink),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedCard(
                        onClick = { showDrinkSelector = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Иконка напитка с анимацией
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            try {
                                                Color(selectedDrink.color.toColorInt())
                                                    .copy(alpha = 0.2f)
                                            } catch (e: Exception) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedDrink.icon,
                                        fontSize = 32.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = selectedDrink.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.drink_attributes, (selectedDrink.hydrationMultiplier * 100).toInt().toString()),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                        if (selectedDrink.containsCaffeine) {
                                            Text("☕", style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (selectedDrink.containsAlcohol) {
                                            Text("🍺", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select drink"
                            )
                        }
                    }

                    // Ввод количества
                    Text(
                        text = stringResource(R.string.enter_amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 4) {
                                amount = it
                                errorMessage = null
                            }
                        },
                        label = { Text(stringResource(R.string.enter_amount_label)) },
                        placeholder = { Text(stringResource(R.string.enter_amount_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        },
                        suffix = { Text("ml") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Селектор времени
                    Text(
                        text = stringResource(R.string.select_time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit time"
                            )
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════════════
                // ACTIONS: Кнопки действий
                // ═══════════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            val amountValue = amount.toIntOrNull()
                            when {
                                amountValue == null || amountValue <= 0 -> {
                                    errorMessage = "Пожалуйста, введите корректную сумму."
                                }
                                amountValue > 3000 -> {
                                    errorMessage = "Объем кажется слишком большим"
                                }
                                else -> {
                                    val dateTime = date.atTime(selectedHour, selectedMinute)
                                    onAddEntry(amountValue, selectedDrink, dateTime)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = amount.isNotBlank(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add))
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DIALOGS: Вложенные диалоги для выбора напитка и времени
    // ═══════════════════════════════════════════════════════════════════════════════

    if (showDrinkSelector) {
        DrinkSelectorDialog(
            drinks = drinks,
            selectedDrink = selectedDrink,
            onDrinkSelected = {
                selectedDrink = it
                showDrinkSelector = false
            },
            onCreateCustomDrink = { showCreateDrink = true },
            onDismiss = { showDrinkSelector = false }
        )
    }

    if (showCreateDrink) {
        CreateCustomDrinkDialog(
            onDrinkCreated = onDrinkCreated,
            onDismiss = { showCreateDrink = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            onTimeSelected = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// КОМПОНЕНТ: Простой пикер времени
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = stringResource(R.string.selected_time),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.hour),
                            style = MaterialTheme.typography.labelMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    selectedHour =
                                        if (selectedHour > 0) selectedHour - 1 else 23
                                }
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = null
                                )
                            }

                            Text(
                                text = String.format("%02d", selectedHour),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = {
                                    selectedHour =
                                        if (selectedHour < 23) selectedHour + 1 else 0
                                }
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.minute),
                            style = MaterialTheme.typography.labelMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    selectedMinute =
                                        if (selectedMinute >= 5) selectedMinute - 5 else 55
                                }
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = null
                                )
                            }

                            Text(
                                text = String.format("%02d", selectedMinute),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = {
                                    selectedMinute =
                                        if (selectedMinute < 55) selectedMinute + 5 else 0
                                }
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Утро" to (8 to 0),
                        "Полдень" to (12 to 0),
                        "Вечер" to (18 to 0),
                        "Ночь" to (21 to 0)
                    ).forEach { (label, time) ->
                        AssistChip(
                            onClick = {
                                selectedHour = time.first
                                selectedMinute = time.second
                            },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onTimeSelected(
                                selectedHour,
                                selectedMinute
                            )
                        }
                    ) {
                        Text(stringResource(R.string.set_time))
                    }
                }
            }
        }
    }
}