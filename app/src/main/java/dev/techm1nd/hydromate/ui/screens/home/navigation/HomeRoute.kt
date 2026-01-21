package dev.techm1nd.hydromate.ui.screens.home.navigation

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
import dev.techm1nd.hydromate.ui.screens.home.HomeScreen
import dev.techm1nd.hydromate.ui.screens.home.HomeViewModel
import dev.techm1nd.hydromate.ui.screens.home.model.HomeEffect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeRoute(
    modifier: Modifier,
    navController: NavHostController
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
//    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                HomeEffect.ShowAddWaterAnimation -> {
                    // Trigger animation
                }
                HomeEffect.ShowGoalReachedCelebration -> {
//                    snackbarHostState.showSnackbar(
//                        message = "🎉 Daily goal reached! Great job!",
//                        duration = SnackbarDuration.Short
//                    )
                }
                is HomeEffect.ShowError -> {
//                    snackbarHostState.showSnackbar(
//                        message = effect.message,
//                        duration = SnackbarDuration.Short
//                    )
                }
                is HomeEffect.ShowSuccess -> {
//                    snackbarHostState.showSnackbar(
//                        message = effect.message,
//                        duration = SnackbarDuration.Short
//                    )
                }
                HomeEffect.HapticFeedback -> {
                    // Trigger haptic feedback
                }
                is HomeEffect.ShowHydrationInfo -> {
                    // Show hydration info
                }
            }
        }
    }

    HomeScreen(
        modifier = modifier,
        state = state,
//        snackbarHostState = snackbarHostState,
        handleIntent = viewModel::handleIntent,
        navController = navController
    )
}