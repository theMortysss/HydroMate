package dev.techm1nd.hydromate.ui.screens.statistics.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.techm1nd.hydromate.ui.screens.statistics.StatisticsScreen
import dev.techm1nd.hydromate.ui.screens.statistics.StatisticsViewModel
import dev.techm1nd.hydromate.ui.screens.statistics.model.StatisticsIntent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StatisticsRoute(
    modifier: Modifier,
    navController: NavHostController
) {
    val viewModel: StatisticsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Handle side effects
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            viewModel.handleIntent(StatisticsIntent.ClearError)
        }
    }

    StatisticsScreen(
        modifier = modifier,
        state = state,
        handleIntent = viewModel::handleIntent,
        navController = navController
    )
}