package dev.techm1nd.hydromate.ui.screens.profile.navigation

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
import dev.techm1nd.hydromate.ui.screens.profile.ProfileScreen
import dev.techm1nd.hydromate.ui.screens.profile.ProfileViewModel
import dev.techm1nd.hydromate.ui.screens.profile.model.ProfileEffect
import dev.techm1nd.hydromate.ui.screens.profile.model.ProfileIntent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileRoute(
    modifier: Modifier,
    navController: NavHostController,
    onNavigateToAuth: () -> Unit,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Handle effects
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ProfileEffect.ShowSuccess -> {

                }

                is ProfileEffect.ShowError -> {

                }

                is ProfileEffect.CharacterUnlocked -> {

                }

                ProfileEffect.LevelUp -> {

                }

                ProfileEffect.NavigateToAuth -> onNavigateToAuth()
            }
        }
    }

    // Handle errors
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            viewModel.handleIntent(ProfileIntent.ClearError)
        }
    }

    ProfileScreen(
        modifier = modifier,
        state = state,
        handleIntent = viewModel::handleIntent,
        navController = navController
    )
}