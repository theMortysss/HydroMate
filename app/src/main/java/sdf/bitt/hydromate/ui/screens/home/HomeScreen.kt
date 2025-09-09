package sdf.bitt.hydromate.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import sdf.bitt.hydromate.ui.components.CharacterDisplay
import sdf.bitt.hydromate.ui.components.ProgressCard
import sdf.bitt.hydromate.ui.components.QuickAddButtons
import sdf.bitt.hydromate.ui.components.TodayEntriesList

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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

            // Progress Card
            ProgressCard(
                currentAmount = uiState.currentAmount,
                goalAmount = uiState.goalAmount,
                progressPercentage = uiState.progressPercentage,
                modifier = Modifier.fillMaxWidth()
            )

            // Quick Add Buttons
            QuickAddButtons(
                amounts = uiState.quickAmounts,
                onAmountClick = { amount ->
                    viewModel.handleIntent(HomeIntent.AddWater(amount))
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
    }
}
