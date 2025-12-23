package dev.techm1nd.hydromate.ui.screens.profile

import dev.techm1nd.hydromate.domain.entities.*
import dev.techm1nd.hydromate.domain.usecases.challenge.CompleteChallengeUseCase

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val achievements: List<Achievement> = emptyList(),
    val activeChallenges: List<Challenge> = emptyList(),
    val completedChallenges: List<Challenge> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

    // Dialogs
    val showCharacterSelection: Boolean = false,
    val showStartChallengeDialog: Boolean = false,
    val showAchievementDetails: Achievement? = null,
    val showChallengeCompletion: CompleteChallengeUseCase.CompletionResult? = null,

    // Auth
    val currentUser: User? = null,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val showAuthDialog: Boolean = false,
    val showLinkAccountDialog: Boolean = false,

    val showEditProfileDialog: Boolean = false,
)

sealed class ProfileIntent {
    object LoadData : ProfileIntent()
    data class SelectCharacter(val character: CharacterType) : ProfileIntent()
    data class StartChallenge(val type: ChallengeType) : ProfileIntent()
    data class AbandonChallenge(val challengeId: String) : ProfileIntent()

    object ShowCharacterSelection : ProfileIntent()
    object HideCharacterSelection : ProfileIntent()
    object ShowStartChallengeDialog : ProfileIntent()
    object HideStartChallengeDialog : ProfileIntent()
    data class ShowAchievementDetails(val achievement: Achievement) : ProfileIntent()
    object HideAchievementDetails : ProfileIntent()
    object HideChallengeCompletion : ProfileIntent()

    data class EditProfile(val newDisplayName: String) : ProfileIntent()
    object ShowEditProfileDialog : ProfileIntent()
    object HideEditProfileDialog : ProfileIntent()

    object SyncNow : ProfileIntent()
    object SignOut : ProfileIntent()
    object ShowLinkAccount : ProfileIntent()
    object HideLinkAccount : ProfileIntent()
    data class LinkWithEmail(val email: String, val password: String) : ProfileIntent()
    data class LinkWithGoogle(val idToken: String) : ProfileIntent()

    object ClearError : ProfileIntent()
}

sealed class ProfileEffect {
    data class ShowSuccess(val message: String) : ProfileEffect()
    data class ShowError(val message: String) : ProfileEffect()
    data class CharacterUnlocked(val character: CharacterType) : ProfileEffect()
    object LevelUp : ProfileEffect()
    object NavigateToAuth : ProfileEffect()
}
