package sdf.bitt.hydromate.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import sdf.bitt.hydromate.ui.components.*

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
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .padding(vertical = 30.dp)
                    .graphicsLayer { shadowElevation = 5f }
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
                .verticalScroll(rememberScrollState()),
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
                val showNetHydration = uiState.showNetHydration
                val hydrationData = uiState.hydrationData

                // Info badge если показываем чистую гидратацию
                if (showNetHydration && hydrationData != null && hydrationData.totalDehydration > 0) {
                    Card(
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
                            Column {
                                Text(
                                    text = "Showing Net Hydration",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Statistics account for caffeine/alcohol dehydration effects",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                        alpha = 0.8f
                                    )
                                )
                            }
                        }
                    }
                }

                // Weekly overview card с учетом настройки
                WeeklyOverviewCardEnhanced(
                    weeklyStats = stats,
                    showNetHydration = showNetHydration,
                    hydrationData = hydrationData,
                    modifier = Modifier.fillMaxWidth()
                )

                // Daily chart с учетом настройки
                DailyWaterChartEnhanced(
                    dailyProgress = stats.dailyProgress,
                    showNetHydration = showNetHydration,
                    modifier = Modifier.fillMaxWidth()
                )

                // Statistics cards
                StatisticsCardsEnhanced(
                    weeklyStats = stats,
                    showNetHydration = showNetHydration,
                    hydrationData = hydrationData,
                    modifier = Modifier.fillMaxWidth()
                )

                // Drink breakdown если есть данные
                hydrationData?.let { data ->
                    if (data.drinkBreakdown.isNotEmpty()) {
                        DrinkBreakdownCard(
                            drinkBreakdown = data.drinkBreakdown,
                            totalAmount = if (showNetHydration) {
                                data.netHydration
                            } else {
                                data.totalActual
                            },
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