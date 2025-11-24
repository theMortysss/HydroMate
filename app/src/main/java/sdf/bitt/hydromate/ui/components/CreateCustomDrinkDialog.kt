package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.DrinkType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomDrinkDialog(
    onDrinkCreated: (Drink) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("💧") }
    var selectedCategory by remember { mutableStateOf(DrinkType.CUSTOM) }
    var hydrationMultiplier by remember { mutableStateOf("100") }
    var containsCaffeine by remember { mutableStateOf(false) }
    var containsAlcohol by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf("#2196F3") }
    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() &&
            hydrationMultiplier.toFloatOrNull()?.let { it in 0f..120f } == true

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
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
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
                    Text(
                        text = "Create Custom Drink",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Drink Name") },
                        placeholder = { Text("e.g., Green Tea") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Icon and Color Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icon Selector
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showIconPicker = true },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = selectedIcon,
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Select Icon",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Color Selector
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showColorPicker = true },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = try {
                                        Color(selectedColor.toColorInt())
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                ) {}
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Select Color",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Category
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(200.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DrinkType.values().toList()) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = category == selectedCategory,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }

                    // Hydration Multiplier
                    Column {
                        Text(
                            text = "Hydration Level (0-120%)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = hydrationMultiplier,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                    hydrationMultiplier = it
                                }
                            },
                            label = { Text("Percentage") },
                            suffix = { Text("%") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = hydrationMultiplier.toFloatOrNull()?.let { it !in 0f..120f } == true
                        )
                        Text(
                            text = "100% = pure water, <100% = less hydrating, >100% = more hydrating",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Properties
                    Text(
                        text = "Properties",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = containsCaffeine,
                            onClick = { containsCaffeine = !containsCaffeine },
                            label = { Text("☕ Contains Caffeine") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = containsAlcohol,
                            onClick = { containsAlcohol = !containsAlcohol },
                            label = { Text("🍺 Contains Alcohol") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (containsCaffeine || containsAlcohol) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("ℹ️", fontSize = 20.sp)
                                Text(
                                    text = when {
                                        containsAlcohol -> "Alcohol causes ~15% dehydration effect"
                                        containsCaffeine -> "Caffeine causes ~5% dehydration effect"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
                            val drink = Drink(
                                name = name.trim(),
                                icon = selectedIcon,
                                hydrationMultiplier = (hydrationMultiplier.toFloatOrNull() ?: 100f) / 100f,
                                category = selectedCategory,
                                containsCaffeine = containsCaffeine,
                                containsAlcohol = containsAlcohol,
                                isCustom = true,
                                color = selectedColor
                            )
                            onDrinkCreated(drink)
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }

    // Icon Picker Dialog
    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = selectedIcon,
            onIconSelected = {
                selectedIcon = it
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = selectedColor,
            onColorSelected = {
                selectedColor = it
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@Composable
private fun CategoryChip(
    category: DrinkType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = category.icon, fontSize = 20.sp)
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun IconPickerDialog(
    currentIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val icons = listOf(
        "💧", "🍵", "☕", "🧃", "🥤", "🥛", "🍺", "🍷", "🍹",
        "🧋", "🍶", "🥃", "🍸", "🧉", "🥥", "🍊", "🍋", "🍎",
        "🍏", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒", "🍑",
        "🥭", "🍍", "🥝", "🍅", "🫒", "🥗", "🍲", "🍜", "🍛"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Icon") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(icons) { icon ->
                    Card(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { onIconSelected(icon) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (icon == currentIcon) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 32.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorPickerDialog(
    currentColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        "#2196F3", "#4CAF50", "#FF9800", "#E91E63", "#9C27B0",
        "#3F51B5", "#00BCD4", "#009688", "#8BC34A", "#CDDC39",
        "#FFC107", "#FF5722", "#795548", "#607D8B", "#F44336"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(colors) { color ->
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { onColorSelected(color) },
                        shape = RoundedCornerShape(12.dp),
                        color = try {
                            Color(color.toColorInt())
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        },
                        border = if (color == currentColor) {
                            BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                        } else null
                    ) {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}