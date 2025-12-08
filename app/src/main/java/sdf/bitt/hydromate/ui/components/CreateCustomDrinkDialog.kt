package sdf.bitt.hydromate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import sdf.bitt.hydromate.domain.entities.AlcoholCategory
import sdf.bitt.hydromate.domain.entities.CaffeineLevel
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

    // Новые поля
    var caffeineContent by remember { mutableStateOf("") }
    var alcoholPercentage by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#2196F3") }

    // UI состояния
    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showSimilarDrinks by remember { mutableStateOf(false) }
    var showHydrationHelp by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() &&
            hydrationMultiplier.toFloatOrNull()?.let { it in -400f..200f } == true

    // Рассчитываем уровни кофеина и алкоголя
    val caffeineMg = caffeineContent.toIntOrNull() ?: 0
    val alcoholPct = alcoholPercentage.toFloatOrNull() ?: 0f
    val caffeineLevel = CaffeineLevel.fromMg(caffeineMg)
    val alcoholCategory = AlcoholCategory.fromPercentage(alcoholPct)

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
                .fillMaxHeight(0.95f)
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
                    // Info Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "💡 Not sure about hydration values?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                TextButton(
                                    onClick = { showSimilarDrinks = true },
                                    modifier = Modifier.padding(0.dp)
                                ) {
                                    Text("Use \"Similar to...\" feature →")
                                }
                            }
                        }
                    }

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Drink Name") },
                        placeholder = { Text("e.g., Green Tea Latte") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Icon and Color Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                Text(text = selectedIcon, fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Select Icon", style = MaterialTheme.typography.bodySmall)
                            }
                        }

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
                                Text("Select Color", style = MaterialTheme.typography.bodySmall)
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

                    Divider()

                    // Hydration Level
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hydration Level",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { showHydrationHelp = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Help",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

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
                            isError = hydrationMultiplier.toFloatOrNull()
                                ?.let { it !in -400f..200f } == true
                        )

                        Text(
                            text = "100% = water, <100% = less hydrating, >100% = more hydrating",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Divider()

                    // Caffeine Content
                    Column {
                        Text(
                            text = "Caffeine Content (optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = caffeineContent,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                    caffeineContent = it
                                }
                            },
                            label = { Text("Caffeine mg per 250ml") },
                            suffix = { Text("mg") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("e.g., 95 for coffee") }
                        )

                        if (caffeineLevel != CaffeineLevel.NONE) {
                            Text(
                                text = "Level: ${caffeineLevel.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Text(
                            text = "Reference: Coffee ~95mg, Tea ~40mg, Energy drink ~80mg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Divider()

                    // Alcohol Percentage
                    Column {
                        Text(
                            text = "Alcohol Content (optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = alcoholPercentage,
                            onValueChange = {
                                if (it.isEmpty() || (it.toFloatOrNull() != null && it.length <= 4)) {
                                    alcoholPercentage = it
                                }
                            },
                            label = { Text("Alcohol percentage") },
                            suffix = { Text("%") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            placeholder = { Text("e.g., 5.0 for beer") }
                        )

                        if (alcoholCategory != AlcoholCategory.NONE) {
                            Text(
                                text = "Category: ${alcoholCategory.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Text(
                            text = "Reference: Beer ~5%, Wine ~12%, Spirits ~40%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Warning if both caffeine and alcohol
                    if (caffeineContent.toIntOrNull() ?: 0 > 0 &&
                        alcoholPercentage.toFloatOrNull() ?: 0f > 0f
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⚠️", fontSize = 20.sp)
                                Text(
                                    text = "This drink contains both caffeine and alcohol. Combined dehydration effects will be calculated.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
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
                                hydrationMultiplier = (hydrationMultiplier.toFloatOrNull()
                                    ?: 100f) / 100f,
                                category = selectedCategory,
                                caffeineContent = caffeineContent.toIntOrNull() ?: 0,
                                alcoholPercentage = alcoholPercentage.toFloatOrNull() ?: 0f,
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

// Dialogs
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

    if (showSimilarDrinks) {
        SimilarDrinksDialog(
            onDrinkSelected = { drink ->
                hydrationMultiplier = (drink.hydrationMultiplier * 100).toInt().toString()
                caffeineContent = drink.caffeineContent.toString()
                alcoholPercentage = drink.alcoholPercentage.toString()
                selectedCategory = drink.category
                showSimilarDrinks = false
            },
            onDismiss = { showSimilarDrinks = false }
        )
    }

    if (showHydrationHelp) {
        HydrationHelpDialog(
            onDismiss = { showHydrationHelp = false }
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
private fun SimilarDrinksDialog(
    onDrinkSelected: (Drink) -> Unit,
    onDismiss: () -> Unit
) {
    val similarDrinks = remember {
        Drink.getDefaultDrinks().filter { !it.isCustom }
    }
    Dialog(onDismissRequest = onDismiss) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Similar to...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Select a drink to copy its hydration values",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(similarDrinks) { drink ->
                        SimilarDrinkItem(
                            drink = drink,
                            onClick = { onDrinkSelected(drink) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimilarDrinkItem(
    drink: Drink,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = drink.icon, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drink.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "${(drink.hydrationMultiplier * 100).toInt()}% hydration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (drink.containsCaffeine) {
                        Text(
                            text = "☕ ${drink.caffeineContent}mg",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (drink.containsAlcohol) {
                        Text(
                            text = "🍺 ${drink.alcoholPercentage}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HydrationHelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("💧", fontSize = 32.sp) },
        title = { Text("Hydration Level Guide") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "How hydrating is your drink compared to water?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                HydrationExample("100%", "Water, herbal tea", "Same as water")
                HydrationExample("95-100%", "Regular tea, milk", "Nearly as good as water")
                HydrationExample("85-95%", "Coffee, juice", "Good hydration")
                HydrationExample("70-85%", "Soup, smoothies", "Moderate hydration")
                HydrationExample("105-110%", "Coconut water, sports drinks", "Enhanced hydration")
                HydrationExample("<70%", "Alcohol, energy drinks", "Poor hydration")

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "💡 Tip: If you're not sure, use the \"Similar to...\" feature to copy values from a similar drink!",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
private fun HydrationExample(percentage: String, examples: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = percentage,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(80.dp)
        )
        Column {
            Text(
                text = examples,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
        "🍓", "🍌", "🍉", "🍇", "🍒", "🫐", "🍈", "🍑", "🍐",
        "🥭", "🍍", "🥝", "🍅", "🫑", "🥗", "🍲", "🍜", "🍛"
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
