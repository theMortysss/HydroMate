package sdf.bitt.hydromate.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import sdf.bitt.hydromate.ui.components.AchievementSummary
import sdf.bitt.hydromate.ui.components.DailyWaterChart
import sdf.bitt.hydromate.ui.components.StatisticsCards
import sdf.bitt.hydromate.ui.components.WeekSelector
import sdf.bitt.hydromate.ui.components.WeeklyOverviewCard
import java.time.format.DateTimeFormatter

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
        modifier = modifier.zIndex(1f),
        hostState = snackbarHostState
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .padding(vertical = 30.dp)
                    .graphicsLayer {
                        shadowElevation = 5f
                    }
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 10.dp)
                    .align(Alignment.TopCenter),
                text = it.visuals.message,
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    if (uiState.isLoading && uiState.weeklyStats == null) {
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

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
                // Weekly overview card
                WeeklyOverviewCard(
                    weeklyStats = stats,
                    modifier = Modifier.fillMaxWidth()
                )

                // Daily chart
                DailyWaterChart(
                    dailyProgress = stats.dailyProgress,
                    modifier = Modifier.fillMaxWidth()
                )

                // Statistics cards
                StatisticsCards(
                    weeklyStats = stats,
                    modifier = Modifier.fillMaxWidth()
                )

                // Achievement summary
                AchievementSummary(
                    weeklyStats = stats,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier.height(96.dp))

        }
    }
}

