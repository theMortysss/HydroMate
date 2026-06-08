package dev.techm1nd.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun GoalSettingDialog(
    currentGoal: Int,
    onGoalSet: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toString()) }
    val isValidGoal = goalText.toIntOrNull()?.let { it in 500..5000 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Установите ежедневную цель") },
        text = {
            Column {
                Text("Введите свою цель по ежедневному потреблению воды (500-5000ml)")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = goalText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 4) {
                            goalText = it
                        }
                    },
                    label = { Text("Цель (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isValidGoal && goalText.isNotEmpty()
                )
                if (!isValidGoal && goalText.isNotEmpty()) {
                    Text(
                        text = "Цель должна быть между 500ml и 5000ml",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    goalText.toIntOrNull()?.let { goal ->
                        onGoalSet(goal)
                    }
                },
                enabled = isValidGoal
            ) {
                Text("Установить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
