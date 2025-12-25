package dev.techm1nd.hydromate.ui.screens.statistics.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dev.techm1nd.hydromate.ui.navigation.Screen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.statisticsScreen(
    modifier: Modifier,
    navController: NavHostController
) = composable(
    route = Screen.Statistics.route,
    enterTransition = { EnterTransition.None },
    exitTransition = { ExitTransition.None }
) {
    StatisticsRoute(
        modifier = modifier,
        navController = navController
    )
}

@Serializable
data object Statistics