package dev.techm1nd.hydromate.ui.screens.settings

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.techm1nd.hydromate.ui.components.*
import dev.techm1nd.hydromate.ui.screens.auth.AuthScreen
import dev.techm1nd.hydromate.ui.screens.auth.model.AuthState
import dev.techm1nd.hydromate.ui.screens.history.model.HistoryIntent
import dev.techm1nd.hydromate.ui.screens.history.model.HistoryState
import dev.techm1nd.hydromate.ui.screens.settings.model.SettingsIntent
import dev.techm1nd.hydromate.ui.screens.settings.model.SettingsState
import dev.techm1nd.hydromate.ui.screens.settings.model.TimePickerType
import dev.techm1nd.hydromate.ui.theme.HydroMateTheme

@Composable
fun SettingsScreen(
    modifier: Modifier,
    state: SettingsState,
    snackbarHostState: SnackbarHostState,
    handleIntent: (SettingsIntent) -> Unit,
    navController: NavHostController
) {
    val hazeState = remember { HazeState() }

    // Handle dialogs
    if (state.showGoalDialog) {
        GoalSettingDialog(
            currentGoal = state.settings.dailyGoal,
            onGoalSet = { goal ->
                handleIntent(SettingsIntent.UpdateDailyGoal(goal))
                handleIntent(SettingsIntent.HideGoalDialog)
            },
            onDismiss = {
                handleIntent(SettingsIntent.HideGoalDialog)
            }
        )
    }

    if (state.showTimePickerDialog) {
        TimePickerDialog(
            type = state.timePickerType,
            currentTime = when (state.timePickerType) {
                TimePickerType.WAKE_UP -> state.settings.wakeUpTime
                TimePickerType.BED_TIME -> state.settings.bedTime
            },
            onTimeSelected = { time ->
                when (state.timePickerType) {
                    TimePickerType.WAKE_UP -> handleIntent(
                        SettingsIntent.UpdateWakeUpTime(time)
                    )
                    TimePickerType.BED_TIME -> handleIntent(
                        SettingsIntent.UpdateBedTime(time)
                    )
                }
                handleIntent(SettingsIntent.HideTimePickerDialog)
            },
            onDismiss = {
                handleIntent(SettingsIntent.HideTimePickerDialog)
            }
        )
    }

    // Profile dialog
    if (state.showProfileDialog) {
        HydrationProfileDialog(
            currentProfile = state.settings.profile,
            currentRecommendedGoal = state.recommendedGoal,
            onProfileUpdated = { newProfile ->
                handleIntent(SettingsIntent.UpdateProfile(newProfile))
                handleIntent(SettingsIntent.HideProfileDialog)
            },
            onCalculateGoal = { profile ->
                handleIntent(SettingsIntent.CalculateRecommendedGoal(profile))
            },
            onDismiss = {
                handleIntent(SettingsIntent.HideProfileDialog)
            }
        )
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

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .hazeSource(hazeState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Header
            SettingsHeader(
                selectedCharacter = state.selectedCharacter
            )

            // Hydration Calculator Section
            HydrationCalculatorCard(
                profile = state.settings.profile,
                recommendedGoal = state.recommendedGoal,
                onProfileClick = {
                    handleIntent(SettingsIntent.ShowProfileDialog)
                }
            )

            // Notification Section
            // The preview doesn't work because of the timer
            NotificationSettingsCard(
                settings = state.settings,
                onSettingsUpdate = { updatedSettings ->
                    handleIntent(SettingsIntent.UpdateSettings(settings = updatedSettings))
                },
                onWakeUpTimeClick ={
                    handleIntent(SettingsIntent.ShowTimePickerDialog(TimePickerType.WAKE_UP))
                },
                onBedTimeClick = {
                    handleIntent(SettingsIntent.ShowTimePickerDialog(TimePickerType.BED_TIME))
                },
            )

            // About Section
            AboutCard()

            Spacer(modifier.height(96.dp))
        }
    }
}

@Composable
@Preview(showBackground = true,)
private fun SettingsScreen_Preview() {
    HydroMateTheme {
        SettingsScreen(
            modifier = Modifier,
            state = SettingsState(),
            snackbarHostState = remember { SnackbarHostState() },
            handleIntent = {},
            navController = rememberNavController()
        )
    }
}