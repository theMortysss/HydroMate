package dev.techm1nd.hydromate.ui.screens.settings.navigation

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
import dev.techm1nd.hydromate.ui.screens.settings.SettingsScreen
import dev.techm1nd.hydromate.ui.screens.settings.SettingsViewModel
import dev.techm1nd.hydromate.ui.screens.settings.model.SettingsIntent

@Composable
fun SettingsRoute(
    modifier: Modifier,
    navController: NavHostController
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.handleIntent(SettingsIntent.ClearError)
        }
    }

    SettingsScreen(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        handleIntent = viewModel::handleIntent,
        navController = navController
    )
}