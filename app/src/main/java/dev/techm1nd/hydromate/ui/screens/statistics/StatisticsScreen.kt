package dev.techm1nd.hydromate.ui.screens.statistics

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import dev.techm1nd.hydromate.ui.components.*

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.handleIntent(StatisticsIntent.ClearError)
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

    if (uiState.isLoading && uiState.weeklyStats == null) {
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Week selector
            WeekSelector(
                selectedWeekStart = uiState.selectedWeekStart,
                onPreviousWeek = {
                    viewModel.handleIntent(StatisticsIntent.PreviousWeek)
                },
                onNextWeek = {
                    viewModel.handleIntent(StatisticsIntent.NextWeek)
                }
            )

            uiState.weeklyStats?.let { stats ->
                val hydrationData = uiState.hydrationData

                // Weekly overview card с учетом настройки
                WeeklyOverviewCard(
                    weeklyStats = stats,
                    hydrationData = hydrationData,
                    modifier = Modifier.fillMaxWidth()
                )

                // Daily chart с учетом настройки
                DailyWaterChartEnhanced(
                    dailyProgress = stats.dailyProgress,
                    modifier = Modifier.fillMaxWidth()
                )

                // Statistics cards
                StatisticsCards(
                    weeklyStats = stats,
                    hydrationData = hydrationData,
                    modifier = Modifier.fillMaxWidth()
                )

                // Drink breakdown если есть данные
                hydrationData?.let { data ->
                    if (data.drinkBreakdown.isNotEmpty()) {
                        DrinkBreakdownCard(
                            drinkBreakdown = data.drinkBreakdown,
                            totalAmount = data.netHydration,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Achievement summary
                AchievementSummary(
                    weeklyStats = stats,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}