package dev.techm1nd.hydromate.ui.screens.history

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.techm1nd.hydromate.ui.components.AddWaterForDateDialog
import dev.techm1nd.hydromate.ui.components.DateDetailsModal
import dev.techm1nd.hydromate.ui.components.MonthSelector
import dev.techm1nd.hydromate.ui.components.MonthlySummary
import dev.techm1nd.hydromate.ui.components.WaterCalendar
import dev.techm1nd.hydromate.ui.screens.auth.AuthScreen
import dev.techm1nd.hydromate.ui.screens.auth.model.AuthState
import dev.techm1nd.hydromate.ui.screens.history.model.HistoryIntent
import dev.techm1nd.hydromate.ui.screens.history.model.HistoryState
import dev.techm1nd.hydromate.ui.screens.statistics.model.StatisticsIntent
import dev.techm1nd.hydromate.ui.screens.statistics.model.StatisticsState
import dev.techm1nd.hydromate.ui.theme.HydroMateTheme

@Composable
fun HistoryScreen(
    modifier: Modifier,
    state: HistoryState,
    snackbarHostState: SnackbarHostState,
    handleIntent: (HistoryIntent) -> Unit,
    navController: NavHostController
) {
    val hazeState = remember { HazeState() }

    // Handle date selection modal
    state.selectedDateProgress?.let { progress ->
        DateDetailsModal(
            date = state.selectedDate!!,
            progress = progress,
            onDismiss = {
                handleIntent(HistoryIntent.ClearSelectedDate)
            },
            onDeleteEntry = { entryId ->
                handleIntent(HistoryIntent.DeleteEntry(entryId))
            },
            onAddMore = { date ->
                handleIntent(HistoryIntent.ShowAddWaterDialog(date))
                handleIntent(HistoryIntent.ClearSelectedDate)
            }
        )
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

    if (state.isLoading && state.monthlyProgress.isEmpty()) {
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

            // Month selector
            MonthSelector(
                selectedMonth = state.selectedMonth,
                onPreviousMonth = {
                    handleIntent(HistoryIntent.PreviousMonth)
                },
                onNextMonth = {
                    handleIntent(HistoryIntent.NextMonth)
                }
            )

            // Calendar с учетом настройки
            WaterCalendar(
                month = state.selectedMonth,
                monthlyProgress = state.monthlyProgress,
                onDateSelected = { date ->
                    handleIntent(HistoryIntent.SelectDate(date))
                },
                onAddWaterClick = { date -> // Для пустых дат открываем диалог добавления
                    handleIntent(HistoryIntent.ShowAddWaterDialog(date))
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Monthly summary с учетом настройки
            MonthlySummary(
                monthlyProgress = state.monthlyProgress.values.toList(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(96.dp))
        }
        if (state.showAddWaterDialog && state.dateForNewEntry != null) {
            AddWaterForDateDialog(
                date = state.dateForNewEntry!!,
                drinks = state.drinks,
                onAddEntry = { amount, drink, time ->
                    handleIntent(
                        HistoryIntent.AddWaterForDate(
                            date = state.dateForNewEntry!!,
                            amount = amount,
                            drink = drink,
                            time = time
                        )
                    )
                },
                onDrinkCreated = { drink ->
                    handleIntent(HistoryIntent.CreateCustomDrink(drink))
                },
                onDismiss = {
                    handleIntent(HistoryIntent.HideAddWaterDialog)
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true,)
private fun HistoryScreen_Preview() {
    HydroMateTheme {
        HistoryScreen(
            modifier = Modifier,
            state = HistoryState(),
            snackbarHostState = remember { SnackbarHostState() },
            handleIntent = {},
            navController = rememberNavController()
        )
    }
}