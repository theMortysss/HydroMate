package dev.techm1nd.hydromate.ui.screens.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.collectLatest
import dev.techm1nd.hydromate.ui.components.*
import java.time.LocalDate

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }

    // Состояния диалогов
    var showAddWaterDialog by remember { mutableStateOf(false) }
    var showEditPresets by remember { mutableStateOf(false) }

    // Handle side effects
    LaunchedEffect(viewModel.effects) {
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
                    // Show hydration info
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
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            tint = HazeTint(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = .7f),
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
                color = MaterialTheme.colorScheme.onPrimary,
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

            HydrationTipsStories(
                viewedTipIds = uiState.viewedTipIds,
                onTipViewed = { tipId ->
                    viewModel.handleIntent(HomeIntent.MarkTipAsViewed(tipId))
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Character Display
            CharacterDisplay(
                characterState = uiState.characterState,
                selectedCharacter = uiState.selectedCharacter, // CHANGED from uiState.userSettings?.selectedCharacter
                modifier = Modifier.fillMaxWidth()
            )

            // Hydration Progress Card
            uiState.todayProgress?.let {
                uiState.hydrationProgress?.let { hydrationProgress ->
                    uiState.totalHydration?.let { totalHydration ->
                        val showNetHydration = uiState.userSettings?.showNetHydration ?: true

                        HydrationProgressCard(
                            hydrationProgress = hydrationProgress,
                            totalHydration = totalHydration,
                            showNetHydration = showNetHydration,
                            modifier = Modifier.fillMaxWidth()
                        )

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

            // Custom Add Water Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddWaterDialog = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Add Custom Entry",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Choose drink type & amount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open custom entry",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Quick Add Buttons (Enhanced with presets)
            uiState.userSettings?.let { settings ->
                QuickAddButtonsEnhanced(
                    presets = settings.quickAddPresets,
                    drinks = uiState.drinks,
                    onPresetClick = { preset, drink ->
                        viewModel.handleIntent(HomeIntent.AddWater(preset.amount, drink))
                    },
                    onEditPresets = { showEditPresets = true },
                    isLoading = uiState.isAddingWater,
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
    if (showAddWaterDialog) {
        AddWaterForDateDialog(
            date = LocalDate.now(),
            drinks = uiState.drinks,
            onAddEntry = { amount, drink, timestamp ->
                viewModel.handleIntent(HomeIntent.AddWater(amount, drink, timestamp))
            },
            onDrinkCreated = { drink ->
                viewModel.handleIntent(HomeIntent.CreateCustomDrink(drink))
            },
            onDismiss = { showAddWaterDialog = false }
        )
    }

    if (showEditPresets) {
        uiState.userSettings?.let { settings ->
            EditQuickPresetsDialog(
                currentPresets = settings.quickAddPresets,
                drinks = uiState.drinks,
                onPresetsChanged = { newPresets ->
                    viewModel.handleIntent(HomeIntent.UpdateQuickPresets(newPresets))
                },
                onDismiss = { showEditPresets = false }
            )
        }
    }
}