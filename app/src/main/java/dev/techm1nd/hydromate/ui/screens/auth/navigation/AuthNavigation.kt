package dev.techm1nd.hydromate.ui.screens.auth.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dev.techm1nd.hydromate.ui.navigation.Screen
import dev.techm1nd.hydromate.ui.screens.history.navigation.HistoryRoute
import kotlinx.serialization.Serializable

fun NavGraphBuilder.authScreen(
    modifier: Modifier,
    navController: NavHostController,
    onNavigateToHome: () -> Unit,
) = composable(
    route = Screen.Auth.route,
    enterTransition = { EnterTransition.None },
    exitTransition = { ExitTransition.None }
) {
    AuthRoute(
        modifier = modifier,
        navController = navController,
        onNavigateToHome = onNavigateToHome
    )
}

@Serializable
data object Auth