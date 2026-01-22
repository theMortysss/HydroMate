package dev.techm1nd.hydromate.ui.screens.onboarding.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dev.techm1nd.hydromate.ui.navigation.Screen
import dev.techm1nd.hydromate.ui.screens.onboarding.OnboardingScreen
import dev.techm1nd.hydromate.ui.screens.onboarding.OnboardingViewModel
import dev.techm1nd.hydromate.ui.screens.onboarding.model.OnboardingEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable

fun NavGraphBuilder.onboardingScreen(
    modifier: Modifier,
    navController: NavHostController,
    onNavigateToHome: () -> Unit,
) = composable(
    route = Screen.Onboarding.route,
    enterTransition = { EnterTransition.None },
    exitTransition = { ExitTransition.None }
) {
    OnboardingRoute(
        modifier = modifier,
        navController = navController,
        onNavigateToHome = onNavigateToHome
    )
}

@Composable
fun OnboardingRoute(
    modifier: Modifier,
    navController: NavHostController,
    onNavigateToHome: () -> Unit,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Handle effects
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                OnboardingEffect.NavigateToHome -> onNavigateToHome()
                is OnboardingEffect.ShowError -> {
                    // Error handled by GlobalSnackbarController
                }
            }
        }
    }

    OnboardingScreen(
        modifier = modifier,
        state = state,
        handleIntent = viewModel::handleIntent,
        navController = navController
    )
}

@Serializable
data object Onboarding