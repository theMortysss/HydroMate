package sdf.bitt.hydromate.ui.screens.history

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
import sdf.bitt.hydromate.ui.components.DateDetailsModal
import sdf.bitt.hydromate.ui.components.MonthSelector
import sdf.bitt.hydromate.ui.components.MonthlySummaryEnhanced
import sdf.bitt.hydromate.ui.components.WaterCalendarEnhanced

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
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
            viewModel.handleIntent(HistoryIntent.ClearError)
        }
    }

    // Handle date selection modal
    uiState.selectedDateProgress?.let { progress ->
        DateDetailsModal(
            date = uiState.selectedDate!!,
            progress = progress,
            showNetHydration = uiState.showNetHydration,
            onDismiss = {
                viewModel.handleIntent(HistoryIntent.ClearSelectedDate)
            }
        )
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
                .verticalScroll(rememberScrollState()),
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
            if (uiState.showNetHydration) {
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
                showNetHydration = uiState.showNetHydration,
                onDateSelected = { date ->
                    viewModel.handleIntent(HistoryIntent.SelectDate(date))
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Monthly summary с учетом настройки
            MonthlySummaryEnhanced(
                monthlyProgress = uiState.monthlyProgress.values.toList(),
                showNetHydration = uiState.showNetHydration,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}