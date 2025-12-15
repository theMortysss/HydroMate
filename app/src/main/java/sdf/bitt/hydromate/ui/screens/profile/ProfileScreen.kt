package sdf.bitt.hydromate.ui.screens.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.collectLatest
import sdf.bitt.hydromate.ui.components.*

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }

    // Handle effects
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ProfileEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is ProfileEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Long
                    )
                }
                is ProfileEffect.CharacterUnlocked -> {
                    snackbarHostState.showSnackbar(
                        message = "🎉 New character unlocked: ${effect.character.displayName}!",
                        duration = SnackbarDuration.Long
                    )
                }
                ProfileEffect.LevelUp -> {
                    snackbarHostState.showSnackbar(
                        message = "🎊 Level Up! You reached level ${uiState.profile.level}!",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.handleIntent(ProfileIntent.ClearError)
        }
    }

    SnackbarHost(
        modifier = Modifier
            .zIndex(1f)
            .padding(vertical = 12.dp, horizontal = 32.dp),
        hostState = snackbarHostState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            tint = HazeTint(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = .7f),
                                blendMode = BlendMode.Src
                            ),
                            blurRadius = 30.dp,
                        )
                    )
                    .border(
                        width = Dp.Hairline,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = .8f),
                                Color.White.copy(alpha = .2f),
                            ),
                        ),
                        shape = CircleShape
                    )
                    .padding(16.dp),
                text = it.visuals.message,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading && uiState.profile.level == 1) {
            // Initial loading
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .hazeSource(hazeState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Profile Header
                ProfileHeaderCard(
                    profile = uiState.profile,
                    onCharacterClick = {
                        viewModel.handleIntent(ProfileIntent.ShowCharacterSelection)
                    }
                )

                // Active Challenges
                ActiveChallengesSection(
                    challenges = uiState.activeChallenges,
                    onStartChallenge = {
                        viewModel.handleIntent(ProfileIntent.ShowStartChallengeDialog)
                    },
                    onAbandonChallenge = { challengeId ->
                        viewModel.handleIntent(ProfileIntent.AbandonChallenge(challengeId))
                    }
                )

                // Achievements
                AchievementsSection(
                    achievements = uiState.achievements,
                    onAchievementClick = { achievement ->
                        viewModel.handleIntent(ProfileIntent.ShowAchievementDetails(achievement))
                    }
                )

                // Completed Challenges History
                if (uiState.completedChallenges.isNotEmpty()) {
                    CompletedChallengesSection(
                        challenges = uiState.completedChallenges
                    )
                }

                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }

    // Dialogs
    if (uiState.showCharacterSelection) {
        CharacterSelectionDialogEnhanced(
            profile = uiState.profile,
            onCharacterSelected = { character ->
                viewModel.handleIntent(ProfileIntent.SelectCharacter(character))
                viewModel.handleIntent(ProfileIntent.HideCharacterSelection)
            },
            onDismiss = {
                viewModel.handleIntent(ProfileIntent.HideCharacterSelection)
            }
        )
    }

    if (uiState.showStartChallengeDialog) {
        StartChallengeDialog(
            onStartChallenge = { type ->
                viewModel.handleIntent(ProfileIntent.StartChallenge(type))
                viewModel.handleIntent(ProfileIntent.HideStartChallengeDialog)
            },
            onDismiss = {
                viewModel.handleIntent(ProfileIntent.HideStartChallengeDialog)
            }
        )
    }

    uiState.showAchievementDetails?.let { achievement ->
        AchievementDetailsDialog(
            achievement = achievement,
            onDismiss = {
                viewModel.handleIntent(ProfileIntent.HideAchievementDetails)
            }
        )
    }

    uiState.showChallengeCompletion?.let { result ->
        ChallengeCompletionDialog(
            result = result,
            onDismiss = {
                viewModel.handleIntent(ProfileIntent.HideChallengeCompletion)
            }
        )
    }
}
