package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.QuickAddPreset

@Composable
fun GoalSettingsCard(
    dailyGoal: Int,
    quickAmounts: List<QuickAddPreset>,
    drinks: List<Drink>,
    onGoalClick: () -> Unit,
    onQuickAmountsEdit: (List<QuickAddPreset>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickAmountsDialog by remember { mutableStateOf(false) }

    SettingsCard(
        title = "Daily Goal",
        icon = "🎯",
        modifier = modifier
    ) {
        // Daily goal setting
        ListItem(
            headlineContent = { Text("Daily Water Goal") },
            supportingContent = { Text("Your target water intake per day") },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${dailyGoal}ml",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit goal",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            modifier = Modifier.clickable { onGoalClick() }
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Quick amounts setting
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quick Add Amounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Customize your quick add buttons",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                TextButton(onClick = { showQuickAmountsDialog = true }) {
                    Text("Edit")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(quickAmounts) { _, amount ->
                    AssistChip(
                        onClick = { },
                        label = { Text("${amount.drinkIcon ?: "💧"} ${amount.amount}ml") },
                        enabled = false
                    )
                }
            }
        }
    }

    if (showQuickAmountsDialog) {
        EditQuickPresetsDialog(
            currentPresets = quickAmounts,
            drinks = drinks,
            onPresetsChanged = onQuickAmountsEdit,
            onDismiss = { showQuickAmountsDialog = false }
        )
    }
}