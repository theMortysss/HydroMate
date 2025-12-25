package dev.techm1nd.hydromate.ui.screens.history.navigation

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
import dev.techm1nd.hydromate.ui.screens.history.HistoryScreen
import dev.techm1nd.hydromate.ui.screens.history.HistoryViewModel
import dev.techm1nd.hydromate.ui.screens.history.model.HistoryIntent

@Composable
fun HistoryRoute(
    modifier: Modifier,
    navController: NavHostController
) {
    val viewModel: HistoryViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.handleIntent(HistoryIntent.ClearError)
        }
    }

    HistoryScreen(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        handleIntent = viewModel::handleIntent,
        navController = navController
    )
}