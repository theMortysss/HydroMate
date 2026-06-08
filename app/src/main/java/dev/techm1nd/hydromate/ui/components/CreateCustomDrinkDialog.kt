package dev.techm1nd.hydromate.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import dev.techm1nd.hydromate.domain.entities.AlcoholCategory
import dev.techm1nd.hydromate.domain.entities.CaffeineLevel
import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.entities.DrinkType
import dev.techm1nd.hydromate.R

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
                        text = "Создать напиток",
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
                                    text = stringResource(R.string.not_sure_how_to_rate),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                TextButton(
                                    onClick = { showSimilarDrinks = true },
                                    modifier = Modifier.padding(0.dp)
                                ) {
                                    Text("Используй похожее →")
                                }
                            }
                        }
                    }

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название") },
                        placeholder = { Text("н-р, Зеленый чай") },
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
                                Text("Иконка", style = MaterialTheme.typography.bodySmall)
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
                                Text("Цвет", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Category
                    Text(
                        text = "Категория",
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
                                text = "Индекс гидратации",
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
                            label = { Text("Процентаж") },
                            suffix = { Text("%") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = hydrationMultiplier.toFloatOrNull()
                                ?.let { it !in -400f..200f } == true
                        )

                        Text(
                            text = "100% = вода, <100% = меньше гидратации, >100% = больше гидратации",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Divider()

                    // Caffeine Content
                    Column {
                        Text(
                            text = "Кофеин (опционально)",
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
                            label = { Text("Кофеин на 250 мл") },
                            suffix = { Text("mg") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("н-р, 95") }
                        )

                        if (caffeineLevel != CaffeineLevel.NONE) {
                            Text(
                                text = "${caffeineLevel.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Divider()

                    // Alcohol Percentage
                    Column {
                        Text(
                            text = "Алкоголь (опционально)",
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
                            label = { Text("Алкоголь в %") },
                            suffix = { Text("%") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            placeholder = { Text("н-р, 5.0") }
                        )

                        if (alcoholCategory != AlcoholCategory.NONE) {
                            Text(
                                text = "${alcoholCategory.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
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
                        Text("Отмена")
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
                        Text("Создать")
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
                        text = "Похоже на...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Выберите напиток, чтобы скопировать показатели гидратации.",
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
                        text = "${(drink.hydrationMultiplier * 100).toInt()}% гидратации",
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
        title = { Text("Выберите иконку") },
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
                Text("Отмена")
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
        title = { Text("Выберите цвет") },
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
                Text("Отмена")
            }
        }
    )
}
