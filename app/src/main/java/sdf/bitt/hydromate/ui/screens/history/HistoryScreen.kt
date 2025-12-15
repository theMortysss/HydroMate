package sdf.bitt.hydromate.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
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
import sdf.bitt.hydromate.ui.components.AddWaterForDateDialog
import sdf.bitt.hydromate.ui.components.DateDetailsModal
import sdf.bitt.hydromate.ui.components.MonthSelector
import sdf.bitt.hydromate.ui.components.MonthlySummaryEnhanced
import sdf.bitt.hydromate.ui.components.WaterCalendarEnhanced
import sdf.bitt.hydromate.ui.screens.home.HomeIntent
import java.time.LocalDate

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
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
            viewModel.handleIntent(HistoryIntent.ClearError)
        }
    }

    // Handle date selection modal
    uiState.selectedDateProgress?.let { progress ->
        DateDetailsModal(
            date = uiState.selectedDate!!,
            progress = progress,
            showNetHydration = uiState.userSettings?.showNetHydration ?: true,
            onDismiss = {
                viewModel.handleIntent(HistoryIntent.ClearSelectedDate)
            },
            onDeleteEntry = { entryId ->
                viewModel.handleIntent(HistoryIntent.DeleteEntry(entryId))
            },
            onAddMore = { date ->
                viewModel.handleIntent(HistoryIntent.ShowAddWaterDialog(date))
                viewModel.handleIntent(HistoryIntent.ClearSelectedDate)
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

    if (uiState.isLoading && uiState.monthlyProgress.isEmpty()) {
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
                selectedMonth = uiState.selectedMonth,
                onPreviousMonth = {
                    viewModel.handleIntent(HistoryIntent.PreviousMonth)
                },
                onNextMonth = {
                    viewModel.handleIntent(HistoryIntent.NextMonth)
                }
            )

            // Info badge если показываем чистую гидратацию
            if (uiState.userSettings?.showNetHydration ?: true) {
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
                        Text(
                            text = "Calendar shows net hydration (accounting for caffeine/alcohol effects)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Calendar с учетом настройки
            WaterCalendarEnhanced(
                month = uiState.selectedMonth,
                monthlyProgress = uiState.monthlyProgress,
                showNetHydration = uiState.userSettings?.showNetHydration ?: true,
                onDateSelected = { date ->
                    viewModel.handleIntent(HistoryIntent.SelectDate(date))
                },
                onAddWaterClick = { date -> // Для пустых дат открываем диалог добавления
                    viewModel.handleIntent(HistoryIntent.ShowAddWaterDialog(date))
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Monthly summary с учетом настройки
            MonthlySummaryEnhanced(
                monthlyProgress = uiState.monthlyProgress.values.toList(),
                showNetHydration = uiState.userSettings?.showNetHydration ?: true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(96.dp))
        }
        if (uiState.showAddWaterDialog && uiState.dateForNewEntry != null) {
            AddWaterForDateDialog(
                date = uiState.dateForNewEntry!!,
                drinks = uiState.drinks,
                onAddEntry = { amount, drink, time ->
                    viewModel.handleIntent(
                        HistoryIntent.AddWaterForDate(
                            date = uiState.dateForNewEntry!!,
                            amount = amount,
                            drink = drink,
                            time = time
                        )
                    )
                },
                onDrinkCreated = { drink ->
                    viewModel.handleIntent(HistoryIntent.CreateCustomDrink(drink))
                },
                onDismiss = {
                    viewModel.handleIntent(HistoryIntent.HideAddWaterDialog)
                }
            )
        }
    }
}