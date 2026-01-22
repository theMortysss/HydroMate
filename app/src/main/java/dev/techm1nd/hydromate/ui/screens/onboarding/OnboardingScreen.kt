package dev.techm1nd.hydromate.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.techm1nd.hydromate.domain.entities.UserProfile
import dev.techm1nd.hydromate.domain.usecases.hydration.RecommendedGoalResult
import dev.techm1nd.hydromate.ui.screens.onboarding.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier,
    state: OnboardingState,
    handleIntent: (OnboardingIntent) -> Unit,
    navController: NavHostController
) {
    val hazeState = remember { HazeState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Welcome to HydroMate",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    if (state.currentStep != OnboardingStep.WELCOME) {
                        IconButton(onClick = { handleIntent(OnboardingIntent.PreviousStep) }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    if (state.currentStep != OnboardingStep.COMPLETE) {
                        TextButton(onClick = { handleIntent(OnboardingIntent.SkipOnboarding) }) {
                            Text("Skip")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .hazeSource(hazeState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Progress indicator
                LinearProgressIndicator(
                    progress = { getProgress(state.currentStep) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Content based on step
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    },
                    label = "onboarding_content"
                ) { step ->
                    when (step) {
                        OnboardingStep.WELCOME -> WelcomeStep()
                        OnboardingStep.PROFILE -> ProfileStep(
                            profile = state.profile,
                            recommendedGoal = state.recommendedGoal,
                            onProfileUpdate = { handleIntent(OnboardingIntent.UpdateProfile(it)) }
                        )
                        OnboardingStep.GOAL -> GoalStep(
                            profile = state.profile,
                            recommendedGoal = state.recommendedGoal,
                            onGoalSelect = { goal, isManual ->
                                handleIntent(OnboardingIntent.SelectGoal(goal, isManual))
                            }
                        )
                        OnboardingStep.COMPLETE -> CompleteStep()
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { handleIntent(OnboardingIntent.NextStep) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                when (state.currentStep) {
                                    OnboardingStep.COMPLETE -> "Get Started"
                                    else -> "Continue"
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "💧",
            fontSize = 120.sp
        )

        Text(
            text = "Stay Hydrated,\nStay Healthy",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Let's personalize your hydration goals based on your lifestyle",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureItem("📊", "Track your daily water intake")
                FeatureItem("🎯", "Set personalized hydration goals")
                FeatureItem("⏰", "Smart reminders throughout the day")
                FeatureItem("🏆", "Earn achievements and unlock characters")
            }
        }
    }
}

@Composable
private fun ProfileStep(
    profile: UserProfile,
    recommendedGoal: RecommendedGoalResult,
    onProfileUpdate: (UserProfile) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tell us about yourself",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "We'll calculate your personalized hydration goal",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Profile editor card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Weight
                OutlinedTextField(
                    value = profile.weightKg.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { weight ->
                            if (weight in 30..200) {
                                onProfileUpdate(profile.copy(weightKg = weight))
                            }
                        }
                    },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Gender
                Text("Gender", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        dev.techm1nd.hydromate.domain.entities.Gender.MALE,
                        dev.techm1nd.hydromate.domain.entities.Gender.FEMALE,
                        dev.techm1nd.hydromate.domain.entities.Gender.PREFER_NOT_TO_SAY
                    ).forEach { gender ->
                        FilterChip(
                            selected = profile.gender == gender,
                            onClick = { onProfileUpdate(profile.copy(gender = gender)) },
                            label = { Text(gender.displayName) }
                        )
                    }
                }

                // Activity Level
                Text("Activity Level", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        dev.techm1nd.hydromate.domain.entities.ActivityLevel.LOW,
                        dev.techm1nd.hydromate.domain.entities.ActivityLevel.MODERATE,
                        dev.techm1nd.hydromate.domain.entities.ActivityLevel.HIGH
                    ).forEach { level ->
                        FilterChip(
                            selected = profile.activityLevel == level,
                            onClick = { onProfileUpdate(profile.copy(activityLevel = level)) },
                            label = { Text(level.displayName) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Climate
                Text("Climate", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        dev.techm1nd.hydromate.domain.entities.Climate.COLD,
                        dev.techm1nd.hydromate.domain.entities.Climate.MODERATE,
                        dev.techm1nd.hydromate.domain.entities.Climate.WARM,
                        dev.techm1nd.hydromate.domain.entities.Climate.HOT
                    ).forEach { climate ->
                        FilterChip(
                            selected = profile.climate == climate,
                            onClick = { onProfileUpdate(profile.copy(climate = climate)) },
                            label = { Text(climate.displayName) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Show recommendation
                if (recommendedGoal.recommendedGoal > 0) {
                    HorizontalDivider()
                    Text(
                        text = "Your recommended goal: ${recommendedGoal.recommendedGoal}ml",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalStep(
    profile: UserProfile,
    recommendedGoal: RecommendedGoalResult,
    onGoalSelect: (Int, Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Choose your daily goal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Recommended goal card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (!profile.isManualGoal)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            ),
            onClick = {
                onGoalSelect(recommendedGoal.recommendedGoal, false)
            }
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recommended",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Based on your profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    if (!profile.isManualGoal) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "${recommendedGoal.recommendedGoal}ml",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (recommendedGoal.explanation.isNotEmpty()) {
                    Text(
                        text = recommendedGoal.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Text(
            text = "OR",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        // Manual goal card
        var manualGoalText by remember { mutableStateOf(profile.manualGoal.toString()) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (profile.isManualGoal)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Set custom goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = manualGoalText,
                    onValueChange = {
                        manualGoalText = it
                        it.toIntOrNull()?.let { goal ->
                            if (goal in 500..5000) {
                                onGoalSelect(goal, true)
                            }
                        }
                    },
                    label = { Text("Daily goal (ml)") },
                    suffix = { Text("ml") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Recommended range: 500ml - 5000ml",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun CompleteStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "🎉",
            fontSize = 120.sp
        )

        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Your personalized hydration journey starts now. Let's make staying hydrated fun and easy!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FeatureItem("💧", "Tap quick add buttons for fast logging")
                FeatureItem("📱", "Enable notifications for reminders")
                FeatureItem("🏆", "Complete challenges to earn XP")
                FeatureItem("🐧", "Unlock new characters as you progress")
            }
        }
    }
}

@Composable
private fun FeatureItem(icon: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 24.sp)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun getProgress(step: OnboardingStep): Float {
    return when (step) {
        OnboardingStep.WELCOME -> 0.25f
        OnboardingStep.PROFILE -> 0.5f
        OnboardingStep.GOAL -> 0.75f
        OnboardingStep.COMPLETE -> 1f
    }
}