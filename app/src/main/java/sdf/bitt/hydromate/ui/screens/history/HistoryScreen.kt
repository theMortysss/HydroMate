package sdf.bitt.hydromate.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import sdf.bitt.hydromate.ui.components.MonthlySummary
import sdf.bitt.hydromate.ui.components.WaterCalendar

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
            onDismiss = {
                viewModel.handleIntent(HistoryIntent.ClearSelectedDate)
            }
        )
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

    if (uiState.isLoading && uiState.monthlyProgress.isEmpty()) {
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

            // Calendar
            WaterCalendar(
                month = uiState.selectedMonth,
                monthlyProgress = uiState.monthlyProgress,
                onDateSelected = { date ->
                    viewModel.handleIntent(HistoryIntent.SelectDate(date))
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Monthly summary
            MonthlySummary(
                monthlyProgress = uiState.monthlyProgress.values.toList(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier.height(96.dp))

    }
}

