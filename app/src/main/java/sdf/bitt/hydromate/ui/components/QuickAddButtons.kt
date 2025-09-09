package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickAddButtons(
    amounts: List<Int>,
    onAmountClick: (Int) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quick Add",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(amounts) { amount ->
                QuickAddButton(
                    amount = amount,
                    onClick = { onAmountClick(amount) },
                    enabled = !isLoading
                )
            }

            item {
                OutlinedButton(
                    onClick = { showCustomDialog = true },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .widthIn(min = 80.dp)
                ) {
                    Text(
                        text = "Custom",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            onAmountSelected = { amount ->
                onAmountClick(amount)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
    }
}

@Composable
private fun QuickAddButton(
    amount: Int,
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
            .height(56.dp)
            .widthIn(min = 80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${amount}ml",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "💧",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun CustomAmountDialog(
    onAmountSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Amount") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } && it.length <= 4) {
                        amountText = it
                    }
                },
                label = { Text("Amount (ml)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amountText.toIntOrNull()?.let { amount ->
                        if (amount > 0) {
                            onAmountSelected(amount)
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
}
