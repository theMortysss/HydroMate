package sdf.bitt.hydromate.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import sdf.bitt.hydromate.domain.entities.*
import sdf.bitt.hydromate.domain.usecases.RecommendedGoalResult

@Composable
fun HydrationCalculatorCard(
    profile: UserProfile,
    recommendedGoal: RecommendedGoalResult?,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        title = "Hydration Calculator",
        icon = "🧮",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mode toggle: Auto vs Manual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (profile.isManualGoal) "Manual Goal" else "Calculated Goal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (profile.isManualGoal) {
                            "You set your own daily goal"
                        } else {
                            "Based on your personal data"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = if (profile.isManualGoal) {
                        "${profile.manualGoal}ml"
                    } else {
                        "${recommendedGoal?.recommendedGoal ?: 2000}ml"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider()

            // Profile summary (только если не ручной режим)
            if (!profile.isManualGoal) {
                ProfileSummaryCard(profile = profile)

                if (recommendedGoal != null && !recommendedGoal.isDefault) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Breakdown
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                alpha = 0.5f
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📊 Calculation Breakdown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = recommendedGoal.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                    alpha = 0.8f
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Healthy range:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "${recommendedGoal.minimumRecommended}-${recommendedGoal.maximumRecommended}ml",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Edit button
            Button(
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (profile.isManualGoal) {
                        "Edit Manual Goal"
                    } else {
                        "Update Profile"
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileSummaryCard(profile: UserProfile) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileItem(
                icon = profile.gender.icon,
                label = "Gender",
                value = profile.gender.displayName
            )
            ProfileItem(
                icon = "⚖️",
                label = "Weight",
                value = "${profile.weightKg} kg"
            )
            ProfileItem(
                icon = profile.activityLevel.icon,
                label = "Activity",
                value = profile.activityLevel.displayName
            )
            ProfileItem(
                icon = profile.climate.icon,
                label = "Climate",
                value = profile.climate.displayName
            )
        }
    }
}

@Composable
private fun ProfileItem(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HydrationProfileDialog(
    currentProfile: UserProfile,
    currentRecommendedGoal: RecommendedGoalResult?,
    onProfileUpdated: (UserProfile) -> Unit,
    onCalculateGoal: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var profile by remember { mutableStateOf(currentProfile) }
    var calculatedGoal by remember { mutableStateOf(currentRecommendedGoal) }

    // Пересчитываем при изменении профиля
    LaunchedEffect(profile) {
        if (!profile.isManualGoal) {
            onCalculateGoal(profile)
        }
    }

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
                    Column {
                        Text(
                            text = "Hydration Profile",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personalize your daily goal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Mode selector
                    ModeSelector(
                        isManualMode = profile.isManualGoal,
                        onModeChange = { isManual ->
                            profile = profile.copy(isManualGoal = isManual)
                        }
                    )

                    AnimatedVisibility(
                        visible = profile.isManualGoal,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ManualGoalInput(
                            currentGoal = profile.manualGoal,
                            onGoalChange = { newGoal ->
                                profile = profile.copy(manualGoal = newGoal)
                            }
                        )
                    }

                    AnimatedVisibility(
                        visible = !profile.isManualGoal,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            // Gender selection
                            GenderSelector(
                                selectedGender = profile.gender,
                                onGenderSelected = { gender ->
                                    profile = profile.copy(gender = gender)
                                }
                            )

                            Divider()

                            // Weight input
                            WeightInput(
                                currentWeight = profile.weightKg,
                                onWeightChange = { weight ->
                                    profile = profile.copy(weightKg = weight)
                                }
                            )

                            Divider()

                            // Activity level
                            ActivityLevelSelector(
                                selectedLevel = profile.activityLevel,
                                onLevelSelected = { level ->
                                    profile = profile.copy(activityLevel = level)
                                }
                            )

                            Divider()

                            // Climate
                            ClimateSelector(
                                selectedClimate = profile.climate,
                                onClimateSelected = { climate ->
                                    profile = profile.copy(climate = climate)
                                }
                            )

                            // Calculated result
                            calculatedGoal?.let { goal ->
                                if (!goal.isDefault) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    CalculatedGoalCard(goal = goal)
                                }
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (profile.isValid()) {
                                onProfileUpdated(profile)
                            }
                        },
                        enabled = profile.isValid(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(
    isManualMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Goal Setting Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeCard(
                icon = "🧮",
                title = "Auto Calculate",
                description = "Based on your profile",
                isSelected = !isManualMode,
                onClick = { onModeChange(false) },
                modifier = Modifier.weight(1f)
            )

            ModeCard(
                icon = "✍️",
                title = "Manual",
                description = "Set your own goal",
                isSelected = isManualMode,
                onClick = { onModeChange(true) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeCard(
    icon: String,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, fontSize = 32.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ManualGoalInput(
    currentGoal: Int,
    onGoalChange: (Int) -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Your Daily Goal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = goalText,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 4) {
                    goalText = it
                    it.toIntOrNull()?.let { goal ->
                        if (goal in 500..5000) {
                            onGoalChange(goal)
                        }
                    }
                }
            },
            label = { Text("Daily goal (ml)") },
            suffix = { Text("ml") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = goalText.toIntOrNull()?.let { it !in 500..5000 } == true,
            supportingText = {
                Text("Recommended range: 1500-3500ml")
            }
        )
    }
}

@Composable
private fun GenderSelector(
    selectedGender: Gender,
    onGenderSelected: (Gender) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Gender",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Gender.values().forEach { gender ->
                SelectableOptionCard(
                    icon = gender.icon,
                    title = gender.displayName,
                    isSelected = gender == selectedGender,
                    onClick = { onGenderSelected(gender) }
                )
            }
        }
    }
}

@Composable
private fun WeightInput(
    currentWeight: Int,
    onWeightChange: (Int) -> Unit
) {
    var weightText by remember { mutableStateOf(currentWeight.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Weight",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = weightText,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 3) {
                    weightText = it
                    it.toIntOrNull()?.let { weight ->
                        if (weight in 30..200) {
                            onWeightChange(weight)
                        }
                    }
                }
            },
            label = { Text("Your weight") },
            suffix = { Text("kg") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = weightText.toIntOrNull()?.let { it !in 30..200 } == true,
            supportingText = {
                Text("Valid range: 30-200kg")
            }
        )
    }
}

@Composable
private fun ActivityLevelSelector(
    selectedLevel: ActivityLevel,
    onLevelSelected: (ActivityLevel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Activity Level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActivityLevel.values().forEach { level ->
                SelectableOptionCard(
                    icon = level.icon,
                    title = level.displayName,
                    isSelected = level == selectedLevel,
                    onClick = { onLevelSelected(level) }
                )
            }
        }
    }
}

@Composable
private fun ClimateSelector(
    selectedClimate: Climate,
    onClimateSelected: (Climate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Climate",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Climate.entries.forEach { climate ->
                SelectableOptionCard(
                    icon = climate.icon,
                    title = climate.displayName,
                    isSelected = climate == selectedClimate,
                    onClick = { onClimateSelected(climate) }
                )
            }
        }
    }
}

@Composable
private fun SelectableOptionCard(
    icon: String,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 28.sp)

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CalculatedGoalCard(goal: RecommendedGoalResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ Recommended Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "${goal.recommendedGoal}ml",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider()

            Text(
                text = goal.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Healthy range:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${goal.minimumRecommended}-${goal.maximumRecommended}ml",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}