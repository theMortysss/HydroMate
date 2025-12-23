package dev.techm1nd.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.techm1nd.hydromate.domain.entities.QuickAddPreset

@Composable
fun QuickAmountsDialog(
    currentAmounts: List<QuickAddPreset>,
    onAmountsChanged: (List<QuickAddPreset>) -> Unit,
    onDismiss: () -> Unit
) {
    val amounts: SnapshotStateList<QuickAddPreset> = remember { mutableStateListOf(*currentAmounts.toTypedArray()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Edit Quick Amounts",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(amounts) { index, amount ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = amount.amount.toString(),
                                onValueChange = { newValue ->
                                    newValue.toIntOrNull()?.let { newAmount ->
                                        if (newAmount in 0..2000) {  // Allow 0 temporarily, filter on save
                                            amounts[index] = amounts[index].copy(amount = newAmount)
                                        }
                                    } ?: run {
                                        // If empty or invalid, set to 0
                                        amounts[index] = amounts[index].copy(amount = 0)
                                    }
                                },
                                label = { Text("Amount") },
                                suffix = { Text("ml") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            if (amounts.size > 1) {
                                IconButton(
                                    onClick = { amounts.removeAt(index) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove"
                                    )
                                }
                            }
                        }
                    }
                }

//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    IconButton(
//                        onClick = {
//                            amounts.add(QuickAddPreset(amount = 100))  // Add default 100ml
//                        }
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Add,
//                            contentDescription = "Add new amount"
//                        )
//                    }
//                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            onAmountsChanged(amounts.filter { it.amount > 0 })
                            onDismiss()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}