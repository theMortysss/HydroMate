package sdf.bitt.hydromate.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.collectLatest
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.ui.components.*

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }

    // Состояния диалогов
    var showDrinkSelector by remember { mutableStateOf(false) }
    var showCreateDrink by remember { mutableStateOf(false) }
    var showHydrationInfo by remember { mutableStateOf(false) }

    // Handle side effects
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                HomeEffect.ShowAddWaterAnimation -> {
                    // Trigger animation
                }

                HomeEffect.ShowGoalReachedCelebration -> {
                    snackbarHostState.showSnackbar(
                        message = "🎉 Daily goal reached! Great job!",
                        duration = SnackbarDuration.Short
                    )
                }

                is HomeEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }

                is HomeEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }

                HomeEffect.HapticFeedback -> {
                    // Trigger haptic feedback
                }

                is HomeEffect.ShowHydrationInfo -> {
                    showHydrationInfo = true
                }
            }
        }
    }

    SnackbarHost(
        modifier = Modifier
            .zIndex(1f)
            .padding(vertical = 12.dp, horizontal = 32.dp),
        hostState = snackbarHostState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = MaterialTheme.colorScheme.background,
                            tint = HazeTint(
                                color = MaterialTheme.colorScheme.background.copy(alpha = .7f),
                                blendMode = BlendMode.Src
                            ),
                            blurRadius = 30.dp,
                        )
                    )
                    .border(
                        width = Dp.Hairline,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = .8f),
                                Color.White.copy(alpha = .2f),
                            ),
                        ),
                        shape = CircleShape
                    )
                    .padding(16.dp),
                text = it.visuals.message,
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .hazeSource(hazeState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Character Display
            CharacterDisplay(
                characterState = uiState.characterState,
                selectedCharacter = uiState.userSettings?.selectedCharacter,
                modifier = Modifier.fillMaxWidth()
            )

            // NEW: Hydration Progress Card с учетом настройки
            uiState.todayProgress?.let { progress ->
                uiState.hydrationProgress?.let { hydrationProgress ->
                    uiState.totalHydration?.let { totalHydration ->
                        val showNetHydration = uiState.userSettings?.showNetHydration ?: true

                        HydrationProgressCard(
                            hydrationProgress = hydrationProgress,
                            totalHydration = totalHydration,
                            showNetHydration = showNetHydration,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Info badge about display mode
                        if (showNetHydration) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                        alpha = 0.5f
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Showing net hydration (accounting for caffeine/alcohol effects)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Drink Selector Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDrinkSelector = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
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
                        Text(
                            text = uiState.selectedDrink?.icon ?: "💧",
                            fontSize = 32.sp
                        )
                        Column {
                            Text(
                                text = "Selected Drink",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                    alpha = 0.7f
                                )
                            )
                            Text(
                                text = uiState.selectedDrink?.name ?: "Water",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Show hydration info
                            val drink = uiState.selectedDrink
                            if (drink != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${(drink.hydrationMultiplier * 100).toInt()}% hydration",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = 0.6f
                                        )
                                    )
                                    if (drink.containsCaffeine) {
                                        Text("☕", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (drink.containsAlcohol) {
                                        Text("🍺", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Change drink",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Quick Add Buttons
            QuickAddButtons(
                amounts = uiState.quickAmounts,
                onAmountClick = { amount ->
                    uiState.selectedDrink?.let { drink ->
                        viewModel.handleIntent(HomeIntent.AddWater(amount, drink))
                    }
                },
                isLoading = uiState.isAddingWater,
                modifier = Modifier.fillMaxWidth()
            )

            // Today's Entries
            uiState.todayProgress?.let { progress ->
                TodayEntriesList(
                    entries = progress.entries,
                    onDeleteEntry = { entryId ->
                        viewModel.handleIntent(HomeIntent.DeleteEntry(entryId))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    // Dialogs
    if (showDrinkSelector) {
        DrinkSelectorDialog(
            drinks = uiState.drinks,
            selectedDrink = uiState.selectedDrink,
            onDrinkSelected = { drink ->
                viewModel.handleIntent(HomeIntent.SelectDrink(drink))
            },
            onCreateCustomDrink = {
                showCreateDrink = true
            },
            onDismiss = { showDrinkSelector = false }
        )
    }

    if (showCreateDrink) {
        CreateCustomDrinkDialog(
            onDrinkCreated = { drink ->
                viewModel.handleIntent(HomeIntent.CreateCustomDrink(drink))
                showCreateDrink = false
            },
            onDismiss = { showCreateDrink = false }
        )
    }
}