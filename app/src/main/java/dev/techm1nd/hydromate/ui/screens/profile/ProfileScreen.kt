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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.techm1nd.hydromate.R
import dev.techm1nd.hydromate.ui.components.*
import dev.techm1nd.hydromate.ui.screens.profile.model.ProfileIntent
import dev.techm1nd.hydromate.ui.screens.profile.model.ProfileState
import dev.techm1nd.hydromate.ui.theme.HydroMateTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

@Composable
fun ProfileScreen(
    modifier: Modifier,
    state: ProfileState,
    handleIntent: (ProfileIntent) -> Unit,
    navController: NavHostController
) {
    val hazeState = remember { HazeState() }
    val isAnonymous = state.currentUser?.isAnonymous == true

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading && state.profile.level == 1) {
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

                // Account Settings - UPDATED with anonymous check
                AccountSettingsCard(
                    currentUser = state.currentUser,
                    syncStatus = state.syncStatus,
                    onSyncNow = {
                        if (!isAnonymous) {
                            handleIntent(ProfileIntent.SyncNow)
                        }
                    },
                    onSignOut = {
                        handleIntent(ProfileIntent.SignOut)
                    },
                    onLinkAccount = {
                        handleIntent(ProfileIntent.ShowLinkAccount)
                    },
                    onEditProfile = {
                        if (!isAnonymous) {
                            handleIntent(ProfileIntent.ShowEditProfileDialog)
                        }
                    },
                    // NEW: Disable sync for anonymous users
                    isSyncEnabled = !isAnonymous
                )

                // Profile Header
                ProfileHeaderCard(
                    profile = state.profile,
                    onCharacterClick = {
                        handleIntent(ProfileIntent.ShowCharacterSelection)
                    }
                )

                // Active Challenges
                ActiveChallengesSection(
                    challenges = state.activeChallenges,
                    onStartChallenge = {
                        handleIntent(ProfileIntent.ShowStartChallengeDialog)
                    },
                    onAbandonChallenge = { challengeId ->
                        handleIntent(ProfileIntent.AbandonChallenge(challengeId))
                    }
                )

                // Achievements
                AchievementsSection(
                    achievements = state.achievements,
                    onAchievementClick = { achievement ->
                        handleIntent(ProfileIntent.ShowAchievementDetails(achievement))
                    }
                )

                // Completed Challenges History
                if (state.completedChallenges.isNotEmpty()) {
                    CompletedChallengesSection(
                        challenges = state.completedChallenges
                    )
                }

                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }

    // Dialogs
    if (state.showEditProfileDialog && !isAnonymous) {
        var newName by remember { mutableStateOf(state.currentUser?.displayName ?: "") }
        AlertDialog(
            onDismissRequest = { handleIntent(ProfileIntent.HideEditProfileDialog) },
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
                    handleIntent(ProfileIntent.EditProfile(newName))
                    handleIntent(ProfileIntent.HideEditProfileDialog)
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { handleIntent(ProfileIntent.HideEditProfileDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.showLinkAccountDialog && isAnonymous) {
        LinkAccountDialog(
            onLinkWithEmail = { email, password ->
                handleIntent(ProfileIntent.LinkWithEmail(email, password))
            },
            onLinkWithGoogle = { idToken ->
                handleIntent(ProfileIntent.LinkWithGoogle(idToken))
            },
            onDismiss = {
                handleIntent(ProfileIntent.HideLinkAccount)
            }
        )
    }

    if (state.showCharacterSelection) {
        CharacterSelectionDialogEnhanced(
            profile = state.profile,
            onCharacterSelected = { character ->
                handleIntent(ProfileIntent.SelectCharacter(character))
                handleIntent(ProfileIntent.HideCharacterSelection)
            },
            onDismiss = {
                handleIntent(ProfileIntent.HideCharacterSelection)
            }
        )
    }

    if (state.showStartChallengeDialog) {
        StartChallengeDialog(
            onStartChallenge = { type ->
                handleIntent(ProfileIntent.StartChallenge(type))
                handleIntent(ProfileIntent.HideStartChallengeDialog)
            },
            onDismiss = {
                handleIntent(ProfileIntent.HideStartChallengeDialog)
            }
        )
    }

    state.showAchievementDetails?.let { achievement ->
        AchievementDetailsDialog(
            achievement = achievement,
            onDismiss = {
                handleIntent(ProfileIntent.HideAchievementDetails)
            }
        )
    }

    state.showChallengeCompletion?.let { result ->
        ChallengeCompletionDialog(
            result = result,
            onDismiss = {
                handleIntent(ProfileIntent.HideChallengeCompletion)
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
    val clientId = stringResource(R.string.default_web_client_id)
    // FIXED: Proper Google Sign-In flow for linking
    fun linkWithGoogle() {
        coroutineScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Allow any Google account
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false) // Don't auto-select for linking
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                // Handle the credential
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        onLinkWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        android.util.Log.e("LinkAccount", "Failed to parse Google ID token", e)
                    }
                }
            } catch (e: GetCredentialException) {
                android.util.Log.e("LinkAccount", "Google Sign-In failed", e)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Your Account") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Save your progress by linking your anonymous account to email or Google.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Google Link Button
                FilledTonalButton(
                    onClick = { linkWithGoogle() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔍", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link with Google")
                }

                Text(
                    text = "OR",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Email Link Section
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    supportingText = { Text("At least 6 characters") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = confirmPassword.isNotBlank() && !passwordsMatch,
                    supportingText = {
                        if (confirmPassword.isNotBlank() && !passwordsMatch) {
                            Text("Passwords don't match", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLinkWithEmail(email, password) },
                enabled = passwordsMatch && email.isNotBlank()
            ) {
                Text("Link with Email")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
private fun ProfileScreen_Preview() {
    HydroMateTheme {
        ProfileScreen(
            modifier = Modifier,
            state = ProfileState(),
            handleIntent = {},
            navController = rememberNavController()
        )
    }
}