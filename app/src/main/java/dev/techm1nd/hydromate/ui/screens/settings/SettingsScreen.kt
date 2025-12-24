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
import dev.techm1nd.hydromate.ui.components.*

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.handleIntent(SettingsIntent.ClearError)
        }
    }

    // Handle dialogs
    if (uiState.showGoalDialog) {
        GoalSettingDialog(
            currentGoal = uiState.settings.dailyGoal,
            onGoalSet = { goal ->
                viewModel.handleIntent(SettingsIntent.UpdateDailyGoal(goal))
                viewModel.handleIntent(SettingsIntent.HideGoalDialog)
            },
            onDismiss = {
                viewModel.handleIntent(SettingsIntent.HideGoalDialog)
            }
        )
    }

//    if (uiState.showCharacterDialog) {
//        CharacterSelectionDialog(
//            currentCharacter = uiState.settings.selectedCharacter,
//            onCharacterSelected = { character ->
//                viewModel.handleIntent(SettingsIntent.UpdateCharacter(character))
//                viewModel.handleIntent(SettingsIntent.HideCharacterDialog)
//            },
//            onDismiss = {
//                viewModel.handleIntent(SettingsIntent.HideCharacterDialog)
//            }
//        )
//    }

    if (uiState.showTimePickerDialog) {
        TimePickerDialog(
            type = uiState.timePickerType,
            currentTime = when (uiState.timePickerType) {
                TimePickerType.WAKE_UP -> uiState.settings.wakeUpTime
                TimePickerType.BED_TIME -> uiState.settings.bedTime
            },
            onTimeSelected = { time ->
                when (uiState.timePickerType) {
                    TimePickerType.WAKE_UP -> viewModel.handleIntent(
                        SettingsIntent.UpdateWakeUpTime(time)
                    )
                    TimePickerType.BED_TIME -> viewModel.handleIntent(
                        SettingsIntent.UpdateBedTime(time)
                    )
                }
                viewModel.handleIntent(SettingsIntent.HideTimePickerDialog)
            },
            onDismiss = {
                viewModel.handleIntent(SettingsIntent.HideTimePickerDialog)
            }
        )
    }

    // Profile dialog
    if (uiState.showProfileDialog) {
        HydrationProfileDialog(
            currentProfile = uiState.settings.profile,
            currentRecommendedGoal = uiState.recommendedGoal,
            onProfileUpdated = { newProfile ->
                viewModel.handleIntent(SettingsIntent.UpdateProfile(newProfile))
                viewModel.handleIntent(SettingsIntent.HideProfileDialog)
            },
            onCalculateGoal = { profile ->
                viewModel.handleIntent(SettingsIntent.CalculateRecommendedGoal(profile))
            },
            onDismiss = {
                viewModel.handleIntent(SettingsIntent.HideProfileDialog)
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

    if (uiState.isLoading) {
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
                selectedCharacter = uiState.selectedCharacter
            )

            // Hydration Calculator Card (вместо обычной Goal Settings Card)
            HydrationCalculatorCard(
                profile = uiState.settings.profile,
                recommendedGoal = uiState.recommendedGoal,
                onProfileClick = {
                    viewModel.handleIntent(SettingsIntent.ShowProfileDialog)
                }
            )

//            // Goal Settings
//            GoalSettingsCard(
//                dailyGoal = uiState.settings.dailyGoal,
//                quickAmounts = uiState.settings.quickAddPresets,
//                drinks = uiState.drinks,
//                onGoalClick = {
//                    viewModel.handleIntent(SettingsIntent.ShowGoalDialog)
//                },
//                onQuickAmountsEdit = { amounts ->
//                    viewModel.handleIntent(SettingsIntent.UpdateQuickAmounts(amounts))
//                }
//            )

            // Hydration Settings
//            HydrationSettingsCard(
//                showNetHydration = uiState.settings.showNetHydration,
//                onShowNetHydrationToggle = { show ->
//                    viewModel.handleIntent(SettingsIntent.UpdateShowNetHydration(show))
//                }
//            )

//            // Character Settings
//            CharacterSettingsCard(
//                selectedCharacter = uiState.settings.selectedCharacter,
//                onCharacterClick = {
//                    viewModel.handleIntent(SettingsIntent.ShowCharacterDialog)
//                }
//            )

            NotificationSettingsCard(
                settings = uiState.settings,
                onSettingsUpdate = { updatedSettings ->
                    viewModel.handleIntent(SettingsIntent.UpdateSettings(settings = updatedSettings))
                },
                onWakeUpTimeClick ={
                    viewModel.handleIntent(SettingsIntent.ShowTimePickerDialog(TimePickerType.WAKE_UP))
                },
                onBedTimeClick = {
                    viewModel.handleIntent(SettingsIntent.ShowTimePickerDialog(TimePickerType.BED_TIME))
                },
            )

//            // Notification Settings (Enhanced version)
//            NotificationSettingsCard(
//                notificationsEnabled = uiState.settings.notificationsEnabled,
//                notificationInterval = uiState.settings.notificationInterval,
//                wakeUpTime = uiState.settings.wakeUpTime,
//                bedTime = uiState.settings.bedTime,
//                onNotificationsToggle = { enabled ->
//                    viewModel.handleIntent(SettingsIntent.UpdateNotifications(enabled))
//                },
//                onIntervalChange = { interval ->
//                    viewModel.handleIntent(SettingsIntent.UpdateNotificationInterval(interval))
//                },
//                onWakeUpTimeClick = {
//                    viewModel.handleIntent(SettingsIntent.ShowTimePickerDialog(TimePickerType.WAKE_UP))
//                },
//                onBedTimeClick = {
//                    viewModel.handleIntent(SettingsIntent.ShowTimePickerDialog(TimePickerType.BED_TIME))
//                }
//            )

            // About Section
            AboutCard()

            Spacer(modifier.height(96.dp))
        }
    }
}