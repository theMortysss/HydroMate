package dev.techm1nd.hydromate.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.entities.*
import dev.techm1nd.hydromate.domain.repositories.ProfileRepository
import dev.techm1nd.hydromate.domain.repositories.SyncRepository
import dev.techm1nd.hydromate.domain.usecases.achievement.CheckAchievementProgressUseCase
import dev.techm1nd.hydromate.domain.usecases.achievement.GetAllAchievementsUseCase
import dev.techm1nd.hydromate.domain.usecases.achievement.UnlockAchievementUseCase
import dev.techm1nd.hydromate.domain.usecases.auth.LinkAnonymousWithEmailUseCase
import dev.techm1nd.hydromate.domain.usecases.auth.LinkAnonymousWithGoogleUseCase
import dev.techm1nd.hydromate.domain.usecases.auth.ObserveAuthStateUseCase
import dev.techm1nd.hydromate.domain.usecases.auth.SignOutUseCase
import dev.techm1nd.hydromate.domain.usecases.auth.UpdateAuthProfileUseCase
import dev.techm1nd.hydromate.domain.usecases.challenge.*
import dev.techm1nd.hydromate.ui.screens.profile.model.ProfileEffect
import dev.techm1nd.hydromate.ui.screens.profile.model.ProfileIntent
import dev.techm1nd.hydromate.ui.screens.profile.model.ProfileState
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val getActiveChallengesUseCase: GetActiveChallengesUseCase,
    private val startChallengeUseCase: StartChallengeUseCase,
    private val completeChallengeUseCase: CompleteChallengeUseCase,
    private val abandonChallengeUseCase: AbandonChallengeUseCase,
    private val checkAchievementProgressUseCase: CheckAchievementProgressUseCase,
    private val unlockAchievementUseCase: UnlockAchievementUseCase,
    private val getAllAchievementsUseCase: GetAllAchievementsUseCase,
    private val getCompletedChallengesUseCase: GetCompletedChallengesUseCase,
    private val updateAuthProfileUseCase: UpdateAuthProfileUseCase,
    private val syncRepository: SyncRepository,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val linkAnonymousWithEmailUseCase: LinkAnonymousWithEmailUseCase,
    private val linkAnonymousWithGoogleUseCase: LinkAnonymousWithGoogleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects: Flow<ProfileEffect> = _effects.receiveAsFlow()

    init {
        loadData()
        observeProfile()
        observeAuthState()
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadData -> loadData()
            is ProfileIntent.SelectCharacter -> selectCharacter(intent.character)
            is ProfileIntent.StartChallenge -> startChallenge(intent.type)
            is ProfileIntent.AbandonChallenge -> abandonChallenge(intent.challengeId)

            ProfileIntent.ShowCharacterSelection -> _state.update {
                it.copy(showCharacterSelection = true)
            }

            ProfileIntent.HideCharacterSelection -> _state.update {
                it.copy(showCharacterSelection = false)
            }

            ProfileIntent.ShowStartChallengeDialog -> _state.update {
                it.copy(showStartChallengeDialog = true)
            }

            ProfileIntent.HideStartChallengeDialog -> _state.update {
                it.copy(showStartChallengeDialog = false)
            }

            is ProfileIntent.ShowAchievementDetails -> _state.update {
                it.copy(showAchievementDetails = intent.achievement)
            }

            ProfileIntent.HideAchievementDetails -> _state.update {
                it.copy(showAchievementDetails = null)
            }

            ProfileIntent.HideChallengeCompletion -> _state.update {
                it.copy(showChallengeCompletion = null)
            }

            ProfileIntent.ClearError -> _state.update { it.copy(error = null) }
            is ProfileIntent.EditProfile -> editProfile(intent.newDisplayName)
            ProfileIntent.ShowEditProfileDialog -> _state.update { it.copy(showEditProfileDialog = true) }
            ProfileIntent.HideEditProfileDialog -> _state.update { it.copy(showEditProfileDialog = false) }
            ProfileIntent.SyncNow -> syncNow()
            ProfileIntent.SignOut -> signOut()
            ProfileIntent.ShowLinkAccount -> _state.update { it.copy(showLinkAccountDialog = true) }
            ProfileIntent.HideLinkAccount -> _state.update { it.copy(showLinkAccountDialog = false) }
            is ProfileIntent.LinkWithEmail -> linkWithEmail(intent.email, intent.password)
            is ProfileIntent.LinkWithGoogle -> linkWithGoogle(intent.idToken)
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { state ->
                when (state) {
                    is AuthState.Authenticated -> _state.update { it.copy(currentUser = state.user) }
                    else -> _state.update { it.copy(currentUser = null) }
                }
            }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            combine(
                profileRepository.getUserProfile(),
                getActiveChallengesUseCase(),
                getAllAchievementsUseCase(),
                getCompletedChallengesUseCase()
            ) { profile, activeChallenges, achievements, completedChallenges ->
                ProfileData(profile, activeChallenges, achievements, completedChallenges)
            }.catch { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load profile data"
                    )
                }
            }.collect { data ->
                _state.update {
                    it.copy(
                        profile = data.profile,
                        activeChallenges = data.activeChallenges,
                        achievements = data.achievements,
                        completedChallenges = data.completedChallenges,
                        isLoading = false
                    )
                }

                // Проверяем завершенные челленджи
                checkCompletedChallenges(data.activeChallenges)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Проверяем прогресс достижений
            checkAchievementProgressUseCase()
                .onSuccess { newlyUnlocked ->
                    newlyUnlocked.forEach { achievement ->
                        _effects.trySend(
                            ProfileEffect.ShowSuccess(
                                "🎉 Achievement unlocked: ${achievement.title}!"
                            )
                        )

                        achievement.unlockableCharacter?.let { character ->
                            _effects.trySend(ProfileEffect.CharacterUnlocked(character))
                        }
                    }
                }
        }
    }

    private fun editProfile(newDisplayName: String) {
        viewModelScope.launch {
            updateAuthProfileUseCase(newDisplayName) // Новый use case: обновляет в Firebase Auth и sync
                .onSuccess { _effects.trySend(ProfileEffect.ShowSuccess("Profile updated")) }
                .onFailure { _effects.trySend(ProfileEffect.ShowError("Failed to update profile")) }
        }
    }

    private fun selectCharacter(character: CharacterType) {
        viewModelScope.launch {
            val currentProfile = _state.value.profile

            if (!currentProfile.isCharacterUnlocked(character)) {
                _effects.trySend(
                    ProfileEffect.ShowError(
                        "Character not unlocked yet! Complete challenges to unlock."
                    )
                )
                return@launch
            }

            val updatedProfile = currentProfile.copy(selectedCharacter = character)
            profileRepository.updateUserProfile(updatedProfile)
                .onSuccess {
                    _effects.trySend(
                        ProfileEffect.ShowSuccess("Character changed to ${character.displayName}!")
                    )
                }
                .onFailure { exception ->
                    _effects.trySend(
                        ProfileEffect.ShowError(
                            exception.message ?: "Failed to change character"
                        )
                    )
                }
        }
    }

    private fun startChallenge(type: ChallengeType) {
        viewModelScope.launch {
            startChallengeUseCase(type)
                .onSuccess { challenge ->
                    _effects.trySend(
                        ProfileEffect.ShowSuccess(
                            "Challenge started! Complete ${challenge.durationDays} days to earn ${challenge.xpReward} XP"
                        )
                    )
                }
                .onFailure { exception ->
                    _effects.trySend(
                        ProfileEffect.ShowError(
                            exception.message ?: "Failed to start challenge"
                        )
                    )
                }
        }
    }

    private fun abandonChallenge(challengeId: String) {
        viewModelScope.launch {
            abandonChallengeUseCase(challengeId)
                .onSuccess {
                    _effects.trySend(
                        ProfileEffect.ShowSuccess("Challenge abandoned")
                    )
                }
                .onFailure { exception ->
                    _effects.trySend(
                        ProfileEffect.ShowError(
                            exception.message ?: "Failed to abandon challenge"
                        )
                    )
                }
        }
    }

    private suspend fun checkCompletedChallenges(challenges: List<Challenge>) {
        val today = java.time.LocalDate.now()

        challenges.forEach { challenge ->
            if (challenge.isActive && !today.isBefore(challenge.endDate)) {
                // Челлендж завершен!
                completeChallengeUseCase(challenge.id)
                    .onSuccess { result ->
                        _state.update { it.copy(showChallengeCompletion = result) }

                        _effects.trySend(
                            ProfileEffect.ShowSuccess(
                                "🎉 Challenge completed! +${result.xpGained} XP"
                            )
                        )

                        result.achievementUnlocked?.let { achievement ->
                            _effects.trySend(
                                ProfileEffect.ShowSuccess(
                                    "🏆 Achievement unlocked: ${achievement.title}!"
                                )
                            )

                            achievement.unlockableCharacter?.let { character ->
                                _effects.trySend(ProfileEffect.CharacterUnlocked(character))
                            }
                        }

                        // Проверяем, повысился ли уровень
                        val currentProfile = _state.value.profile
                        if (result.challenge.xpReward > 0) {
                            _effects.trySend(ProfileEffect.LevelUp)
                        }
                    }
            }
        }
    }

    private fun syncNow() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            syncRepository.syncAll()
                .onSuccess {
                    _effects.trySend(ProfileEffect.ShowSuccess("Sync completed"))
                }
                .onFailure { e ->
                    _effects.trySend(ProfileEffect.ShowError(e.message ?: "Sync failed"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            signOutUseCase()
                .onSuccess {
                    _effects.trySend(ProfileEffect.ShowSuccess("Signed out"))
                    _effects.trySend(ProfileEffect.NavigateToAuth)  // Для навигации в effects
                }
                .onFailure { e ->
                    _effects.trySend(ProfileEffect.ShowError(e.message ?: "Sign out failed"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun linkWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = linkAnonymousWithEmailUseCase(email, password)) {
                is AuthResult.Success -> {
                    _effects.trySend(ProfileEffect.ShowSuccess("Account linked"))
                    _state.update { it.copy(showLinkAccountDialog = false) }
                }

                is AuthResult.Error -> {
                    _effects.trySend(ProfileEffect.ShowError(result.message))
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun linkWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = linkAnonymousWithGoogleUseCase(idToken)) {
                is AuthResult.Success -> {
                    _effects.trySend(ProfileEffect.ShowSuccess("Account linked"))
                    _state.update { it.copy(showLinkAccountDialog = false) }
                }

                is AuthResult.Error -> {
                    _effects.trySend(ProfileEffect.ShowError(result.message))
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private data class ProfileData(
        val profile: UserProfile,
        val activeChallenges: List<Challenge>,
        val achievements: List<Achievement>,
        val completedChallenges: List<Challenge>
    )
}
