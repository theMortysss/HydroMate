package dev.techm1nd.hydromate.ui.screens.profile.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dev.techm1nd.hydromate.ui.navigation.Screen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.profileScreen(
    modifier: Modifier,
    navController: NavHostController,
    onNavigateToAuth: () -> Unit,
) = composable(
    route = Screen.Profile.route,
    enterTransition = { EnterTransition.None },
    exitTransition = { ExitTransition.None }
) {
    ProfileRoute(
        modifier = modifier,
        navController = navController,
        onNavigateToAuth = onNavigateToAuth,
    )
}

@Serializable
data object Profile