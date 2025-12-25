package dev.techm1nd.hydromate.ui.screens.settings.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dev.techm1nd.hydromate.ui.navigation.Screen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.settingsScreen(
    modifier: Modifier,
    navController: NavHostController
) = composable(
    route = Screen.Settings.route,
    enterTransition = { EnterTransition.None },
    exitTransition = { ExitTransition.None }
) {
    SettingsRoute(
        modifier = modifier,
        navController = navController
    )
}

@Serializable
data object Settings