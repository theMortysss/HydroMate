package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.QuickAddPreset

@Composable
fun QuickAddButtonsEnhanced(
    presets: List<QuickAddPreset>,
    drinks: List<Drink>,
    onPresetClick: (QuickAddPreset, Drink) -> Unit,
    onEditPresets: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Add",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            TextButton(onClick = onEditPresets) {
                Text("Edit")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(presets.sortedBy { it.order }) { preset ->
                val drink = drinks.firstOrNull { it.id == preset.drinkId } ?: Drink.WATER

                QuickAddPresetButton(
                    preset = preset,
                    drink = drink,
                    onClick = { onPresetClick(preset, drink) },
                    enabled = !isLoading
                )
            }

            item {
                OutlinedButton(
                    onClick = { showCustomDialog = true },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(72.dp)
                        .widthIn(min = 80.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Custom amount",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Custom",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            drinks = drinks,
            onAmountSelected = { amount, drink ->
                val customPreset = QuickAddPreset(
                    amount = amount,
                    drinkId = drink.id,
                    drinkName = drink.name,
                    drinkIcon = drink.icon
                )
                onPresetClick(customPreset, drink)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
    }
}

@Composable
private fun QuickAddPresetButton(
    preset: QuickAddPreset,
    drink: Drink,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .height(72.dp)
            .widthIn(min = 90.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = drink.icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${preset.amount}ml",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = drink.name,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CustomAmountDialog(
    drinks: List<Drink>,
    onAmountSelected: (Int, Drink) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedDrink by remember { mutableStateOf(drinks.firstOrNull { it.id == 1L } ?: Drink.WATER) }
    var showDrinkSelector by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Amount") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Выбор напитка
                OutlinedCard(
                    onClick = { showDrinkSelector = true },
                    modifier = Modifier.fillMaxWidth()
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
                            Text(
                                text = selectedDrink.icon,
                                fontSize = 28.sp
                            )
                            Column {
                                Text(
                                    text = "Drink Type",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = selectedDrink.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Change drink"
                        )
                    }
                }

                // Ввод количества
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 4) {
                            amountText = it
                        }
                    },
                    label = { Text("Amount (ml)") },
                    placeholder = { Text("e.g., 350") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amountText.toIntOrNull()?.let { amount ->
                        if (amount > 0) {
                            onAmountSelected(amount, selectedDrink)
                        }
                    }
                },
                enabled = amountText.toIntOrNull()?.let { it > 0 } == true
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDrinkSelector) {
        SimpleDrinkSelectorDialog(
            drinks = drinks,
            selectedDrink = selectedDrink,
            onDrinkSelected = {
                selectedDrink = it
                showDrinkSelector = false
            },
            onDismiss = { showDrinkSelector = false }
        )
    }
}

@Composable
internal fun SimpleDrinkSelectorDialog(
    drinks: List<Drink>,
    selectedDrink: Drink,
    onDrinkSelected: (Drink) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Drink") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(drinks.size) { index ->
                    val drink = drinks[index]
                    Card(
                        onClick = { onDrinkSelected(drink) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (drink.id == selectedDrink.id) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = drink.icon, fontSize = 24.sp)
                            Text(
                                text = drink.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}