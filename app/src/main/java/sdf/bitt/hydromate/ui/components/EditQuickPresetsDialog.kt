package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.QuickAddPreset

@Composable
fun EditQuickPresetsDialog(
    currentPresets: List<QuickAddPreset>,
    drinks: List<Drink>,
    onPresetsChanged: (List<QuickAddPreset>) -> Unit,
    onDismiss: () -> Unit
) {
    var presets by remember { mutableStateOf(currentPresets.toMutableList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPreset by remember { mutableStateOf<QuickAddPreset?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
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
                    Text(
                        text = "Quick Add Presets",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "ℹ️ Create quick shortcuts for your favorite drinks and amounts. Maximum 6 presets.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(presets) { index, preset ->
                        val drink = drinks.firstOrNull { it.id == preset.drinkId } ?: Drink.WATER

                        PresetItem(
                            preset = preset,
                            drink = drink,
                            onEdit = { editingPreset = preset },
                            onDelete = {
                                presets.removeAt(index)
                                presets = presets.toMutableList()
                            }
                        )
                    }

                    if (presets.size < 6) {
                        item {
                            OutlinedButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Preset")
                            }
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
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
                            // Проверка на дубликаты
                            val validated = validateAndRemoveDuplicates(presets)
                            onPresetsChanged(validated)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = presets.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showAddDialog || editingPreset != null) {
        AddEditPresetDialog(
            preset = editingPreset,
            drinks = drinks,
            existingPresets = presets,
            onSave = { newPreset ->
                if (editingPreset != null) {
                    val index = presets.indexOfFirst { it.id == editingPreset!!.id }
                    if (index != -1) {
                        presets[index] = newPreset
                        presets = presets.toMutableList()
                    }
                } else {
                    presets.add(newPreset.copy(order = presets.size))
                    presets = presets.toMutableList()
                }
                showAddDialog = false
                editingPreset = null
            },
            onDismiss = {
                showAddDialog = false
                editingPreset = null
            }
        )
    }
}

@Composable
private fun PresetItem(
    preset: QuickAddPreset,
    drink: Drink,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = drink.icon,
                    fontSize = 32.sp
                )
                Column {
                    Text(
                        text = "${preset.amount}ml",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = drink.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
private fun AddEditPresetDialog(
    preset: QuickAddPreset?,
    drinks: List<Drink>,
    existingPresets: List<QuickAddPreset>,
    onSave: (QuickAddPreset) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(preset?.amount?.toString() ?: "") }
    var selectedDrink by remember {
        mutableStateOf(
            drinks.firstOrNull { it.id == preset?.drinkId }
                ?: drinks.firstOrNull { it.id == 1L }
                ?: Drink.WATER
        )
    }
    var showDrinkSelector by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (preset != null) "Edit Preset" else "Add Preset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Drink selector
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
                            Text(text = selectedDrink.icon, fontSize = 28.sp)
                            Column {
                                Text(
                                    text = "Drink",
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
                    }
                }

                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 4) {
                            amount = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Amount (ml)") },
                    placeholder = { Text("e.g., 250") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toIntOrNull()
                    when {
                        amountValue == null || amountValue <= 0 -> {
                            errorMessage = "Please enter a valid amount"
                        }
                        isDuplicate(amountValue, selectedDrink.id, preset, existingPresets) -> {
                            errorMessage = "This preset already exists"
                        }
                        else -> {
                            val newPreset = QuickAddPreset(
                                id = preset?.id ?: java.util.UUID.randomUUID().toString(),
                                amount = amountValue,
                                drinkId = selectedDrink.id,
                                drinkName = selectedDrink.name,
                                drinkIcon = selectedDrink.icon,
                                order = preset?.order ?: 0
                            )
                            onSave(newPreset)
                        }
                    }
                }
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

/**
 * Проверка на дубликаты: одинаковый напиток + количество
 */
private fun isDuplicate(
    amount: Int,
    drinkId: Long,
    editingPreset: QuickAddPreset?,
    existingPresets: List<QuickAddPreset>
): Boolean {
    return existingPresets.any { preset ->
        preset.id != editingPreset?.id && // Не считаем сам редактируемый пресет
                preset.amount == amount &&
                preset.drinkId == drinkId
    }
}

/**
 * Удаляет дубликаты и пересчитывает order
 */
private fun validateAndRemoveDuplicates(presets: List<QuickAddPreset>): List<QuickAddPreset> {
    return presets
        .distinctBy { "${it.amount}-${it.drinkId}" } // Уникальность по паре amount+drinkId
        .mapIndexed { index, preset -> preset.copy(order = index) }
}