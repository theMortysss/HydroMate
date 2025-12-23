package dev.techm1nd.hydromate.ui.screens.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.techm1nd.hydromate.R
import kotlinx.coroutines.flow.collectLatest
import dev.techm1nd.hydromate.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
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
                ProfileEffect.NavigateToAuth -> onNavigateToAuth()
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

                // Account Settings
                AccountSettingsCard(
                    currentUser = uiState.currentUser,
                    syncStatus = uiState.syncStatus,
                    onSyncNow = {
                        viewModel.handleIntent(ProfileIntent.SyncNow)
                    },
                    onSignOut = {
                        viewModel.handleIntent(ProfileIntent.SignOut)
                    },
                    onLinkAccount = {
                        viewModel.handleIntent(ProfileIntent.ShowLinkAccount)
                    },
                    onEditProfile = { viewModel.handleIntent(ProfileIntent.ShowEditProfileDialog) }
                )

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
    if (uiState.showEditProfileDialog) {
        var newName by remember { mutableStateOf(uiState.currentUser?.displayName ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(ProfileIntent.HideEditProfileDialog) },
            title = { Text("Edit Profile") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Display Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.handleIntent(ProfileIntent.EditProfile(newName))
                    viewModel.handleIntent(ProfileIntent.HideEditProfileDialog)
                }) { Text("Save") }
            }
        )
    }

    if (uiState.showLinkAccountDialog) {
        LinkAccountDialog(
            onLinkWithEmail = { email, password ->
                viewModel.handleIntent(ProfileIntent.LinkWithEmail(email, password))
            },
            onLinkWithGoogle = { idToken ->
                viewModel.handleIntent(ProfileIntent.LinkWithGoogle(idToken))
            },
            onDismiss = {
                viewModel.handleIntent(ProfileIntent.HideLinkAccount)
            }
        )
    }

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

@Composable
private fun LinkAccountDialog(
    onLinkWithEmail: (String, String) -> Unit,
    onLinkWithGoogle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val passwordsMatch = password == confirmPassword && password.length >= 6

    // Function to build Google ID request
    fun buildGoogleIdRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // For linking, allow new accounts
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    // Handle Google credential
    fun handleGoogleCredential(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                onLinkWithGoogle(googleIdTokenCredential.idToken)
            } catch (e: GoogleIdTokenParsingException) {
                // Handle parsing error (show snackbar or log)
            }
        }
    }

    // Trigger Google Sign-In
    fun linkWithGoogle() {
        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = buildGoogleIdRequest(),
                    context = context
                )
                handleGoogleCredential(result)
            } catch (e: GetCredentialException) {
                // Handle exceptions (e.g., no credential, cancellation)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = confirmPassword.isNotBlank() && !passwordsMatch
                )
                Button(onClick = { linkWithGoogle() }) {
                    Text("Link with Google")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onLinkWithEmail(email, password) },
                enabled = passwordsMatch && email.isNotBlank()
            ) { Text("Link with Email") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
