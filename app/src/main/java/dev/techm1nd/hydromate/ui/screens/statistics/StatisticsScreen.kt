package dev.techm1nd.hydromate.ui.screens.statistics

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import dev.techm1nd.hydromate.ui.screens.auth.AuthScreen
import dev.techm1nd.hydromate.ui.screens.auth.model.AuthState
import dev.techm1nd.hydromate.ui.screens.home.model.HomeIntent
import dev.techm1nd.hydromate.ui.screens.statistics.model.StatisticsIntent
import dev.techm1nd.hydromate.ui.screens.statistics.model.StatisticsState
import dev.techm1nd.hydromate.ui.theme.HydroMateTheme

@Composable
fun StatisticsScreen(
    modifier: Modifier,
    state: StatisticsState,
    snackbarHostState: SnackbarHostState,
    handleIntent: (StatisticsIntent) -> Unit,
    navController: NavHostController
) {
    val hazeState = remember { HazeState() }

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Week selector
            WeekSelector(
                selectedWeekStart = state.selectedWeekStart,
                onPreviousWeek = {
                    handleIntent(StatisticsIntent.PreviousWeek)
                },
                onNextWeek = {
                    handleIntent(StatisticsIntent.NextWeek)
                }
            )

            // Weekly overview card с учетом настройки
            WeeklyOverviewCard(
                weeklyStats = state.weeklyStats,
                hydrationData = state.hydrationData,
                modifier = Modifier.fillMaxWidth()
            )

            // Daily chart с учетом настройки
            DailyWaterChartEnhanced(
                dailyProgress = state.weeklyStats.dailyProgress,
                modifier = Modifier.fillMaxWidth()
            )

            // Statistics cards
            StatisticsCards(
                weeklyStats = state.weeklyStats,
                hydrationData = state.hydrationData,
                modifier = Modifier.fillMaxWidth()
            )

            // Drink breakdown если есть данные
            if (state.hydrationData.drinkBreakdown.isNotEmpty()) {
                DrinkBreakdownCard(
                    drinkBreakdown = state.hydrationData.drinkBreakdown,
                    totalAmount = state.hydrationData.netHydration,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Achievement summary
            AchievementSummary(
                weeklyStats = state.weeklyStats,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
@Preview(showBackground = true,)
private fun StatisticsScreen_Preview() {
    HydroMateTheme {
        StatisticsScreen(
            modifier = Modifier,
            state = StatisticsState(),
            snackbarHostState = remember { SnackbarHostState() },
            handleIntent = {},
            navController = rememberNavController()
        )
    }
}
