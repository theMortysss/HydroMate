package dev.techm1nd.hydromate.ui.screens.auth.navigation

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
import dev.techm1nd.hydromate.ui.screens.auth.AuthScreen
import dev.techm1nd.hydromate.ui.screens.auth.AuthViewModel
import dev.techm1nd.hydromate.ui.screens.auth.model.AuthEffect
import dev.techm1nd.hydromate.ui.screens.history.HistoryScreen
import dev.techm1nd.hydromate.ui.screens.history.HistoryViewModel
import dev.techm1nd.hydromate.ui.screens.history.model.HistoryIntent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthRoute(
    modifier: Modifier,
    navController: NavHostController,
    onNavigateToHome: () -> Unit,
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    // Handle effects
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AuthEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is AuthEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Long
                    )
                }
                AuthEffect.NavigateToHome -> {
                    onNavigateToHome()
                }
                is AuthEffect.NavigateToGoogleSignIn -> {
                    // Launch Google Sign-In
                }
            }
        }
    }

    AuthScreen(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        handleIntent = viewModel::handleIntent,
        navController = navController
    )
}