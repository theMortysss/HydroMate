package sdf.bitt.hydromate.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.flow.collectLatest
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.ui.components.CharacterDisplay
import sdf.bitt.hydromate.ui.components.CreateCustomDrinkDialog
import sdf.bitt.hydromate.ui.components.DrinkSelectorDialog
import sdf.bitt.hydromate.ui.components.HydrationProgressCard
import sdf.bitt.hydromate.ui.components.ProgressCard
import sdf.bitt.hydromate.ui.components.QuickAddButtons
import sdf.bitt.hydromate.ui.components.TodayEntriesList

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drinks by viewModel.drinks.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showDrinkSelector by remember { mutableStateOf(false) }
    var showCreateDrink by remember { mutableStateOf(false) }


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

                HomeEffect.HapticFeedback -> {
                    // Trigger haptic feedback
                }

                is HomeEffect.ShowHydrationInfo -> {
                }
                is HomeEffect.ShowSuccess -> {

                }
            }
        }
    }

    val hazeState = remember { HazeState() }
    
    SnackbarHost(
        modifier = Modifier
            .zIndex(1f)
            .padding(vertical = 12.dp, horizontal = 32.dp),
        hostState = snackbarHostState
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .hazeChild(state = hazeState, shape = CircleShape)
                    .border(
                        width = Dp.Hairline,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = .8f),
                                Color.White.copy(alpha = .2f),
                            ),
                        ),
                        shape = CircleShape
                    ),
                text = it.visuals.message,
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize(),
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
                .haze(
                    hazeState,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    tint = Color.Black.copy(alpha = .2f),
                    blurRadius = 30.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Character Display
            CharacterDisplay(
                characterState = uiState.characterState,
                selectedCharacter = uiState.userSettings?.selectedCharacter,
                modifier = Modifier.fillMaxWidth()
            )

            // Hydration Progress Card вместо старого ProgressCard
            uiState.hydrationProgress?.let { progress ->
                uiState.totalHydration?.let { hydration ->
                    HydrationProgressCard(
                        hydrationProgress = progress,
                        totalHydration = hydration
                    )
                }
            }

            // NEW: Drink Selector Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDrinkSelector = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
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
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = uiState.selectedDrink?.name ?: "Water",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Change drink"
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
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            Spacer(modifier.height(96.dp))

        }
        // Dialogs
        if (showDrinkSelector) {
            DrinkSelectorDialog(
                drinks = uiState.drinks,
                selectedDrink = uiState.selectedDrink,
                onDrinkSelected = { viewModel.handleIntent(HomeIntent.SelectDrink(it)) },
                onCreateCustomDrink = { showCreateDrink = true },
                onDismiss = { showDrinkSelector = false }
            )
        }

        if (showCreateDrink) {
            CreateCustomDrinkDialog(
                onDrinkCreated = { viewModel.handleIntent(HomeIntent.CreateCustomDrink(it)) },
                onDismiss = { showCreateDrink = false }
            )
        }
    }
}
