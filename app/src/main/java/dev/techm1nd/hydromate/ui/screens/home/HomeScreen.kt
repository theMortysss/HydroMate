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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.techm1nd.hydromate.ui.components.*
import dev.techm1nd.hydromate.ui.screens.home.model.HomeIntent
import dev.techm1nd.hydromate.ui.screens.home.model.HomeState
import dev.techm1nd.hydromate.ui.theme.HydroMateTheme
import java.time.LocalDate

@Composable
fun HomeScreen(
    modifier: Modifier,
    state: HomeState,
    handleIntent: (HomeIntent) -> Unit,
    navController: NavHostController
) {
    val hazeState = remember { HazeState() }

    // Состояния диалогов
    var showAddWaterDialog by remember { mutableStateOf(false) }
    var showEditPresets by remember { mutableStateOf(false) }

    if (state.isLoading) {
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

            // Hydration Progress Card
            HydrationProgressCard(
                hydrationProgress = state.hydrationProgress,
                totalHydration = state.totalHydration,
                characterState = state.characterState,
                selectedCharacter = state.selectedCharacter,
                modifier = Modifier.fillMaxWidth()
            )

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
            QuickAddButtons(
                presets = state.userSettings.quickAddPresets,
                drinks = state.drinks,
                onPresetClick = { preset, drink ->
                    handleIntent(HomeIntent.AddWater(preset.amount, drink))
                },
                onEditPresets = { showEditPresets = true },
                isLoading = state.isAddingWater,
                modifier = Modifier.fillMaxWidth()
            )

            // Hydration Tips
            HydrationTipsStories(
                viewedTipIds = state.viewedTipIds,
                onTipViewed = { tipId ->
                    handleIntent(HomeIntent.MarkTipAsViewed(tipId))
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Today's Entries
            TodayEntriesList(
                entries = state.todayProgress.entries,
                onDeleteEntry = { entryId ->
                    handleIntent(HomeIntent.DeleteEntry(entryId))
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    // Dialogs
    if (showAddWaterDialog) {
        AddWaterForDateDialog(
            date = LocalDate.now(),
            drinks = state.drinks,
            onAddEntry = { amount, drink, timestamp ->
                handleIntent(HomeIntent.AddWater(amount, drink, timestamp))
            },
            onDrinkCreated = { drink ->
                handleIntent(HomeIntent.CreateCustomDrink(drink))
            },
            onDismiss = { showAddWaterDialog = false }
        )
    }

    if (showEditPresets) {
        EditQuickPresetsDialog(
            currentPresets = state.userSettings.quickAddPresets,
            drinks = state.drinks,
            onPresetsChanged = { newPresets ->
                handleIntent(HomeIntent.UpdateQuickPresets(newPresets))
            },
            onDismiss = { showEditPresets = false }
        )
    }
}

@Composable
@Preview(showBackground = true,)
private fun MainScreen_Preview() {
    HydroMateTheme {
        HomeScreen(
            modifier = Modifier,
            state = HomeState(),
//            snackbarHostState = remember { SnackbarHostState() },
            handleIntent = {},
            navController = rememberNavController()
        )
    }
}