package dev.techm1nd.hydromate.ui.components

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
import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.entities.QuickAddPreset

@Composable
fun QuickAddButtons(
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
        }
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
