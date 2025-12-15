package sdf.bitt.hydromate.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.entities.*
import sdf.bitt.hydromate.domain.repositories.ProfileRepository
import sdf.bitt.hydromate.domain.usecases.achievement.CheckAchievementProgressUseCase
import sdf.bitt.hydromate.domain.usecases.achievement.GetAllAchievementsUseCase
import sdf.bitt.hydromate.domain.usecases.achievement.UnlockAchievementUseCase
import sdf.bitt.hydromate.domain.usecases.challenge.*
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
    private val getCompletedChallengesUseCase: GetCompletedChallengesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects: Flow<ProfileEffect> = _effects.receiveAsFlow()

    init {
        loadData()
        observeProfile()
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadData -> loadData()
            is ProfileIntent.SelectCharacter -> selectCharacter(intent.character)
            is ProfileIntent.StartChallenge -> startChallenge(intent.type)
            is ProfileIntent.AbandonChallenge -> abandonChallenge(intent.challengeId)

            ProfileIntent.ShowCharacterSelection -> _uiState.update {
                it.copy(showCharacterSelection = true)
            }
            ProfileIntent.HideCharacterSelection -> _uiState.update {
                it.copy(showCharacterSelection = false)
            }
            ProfileIntent.ShowStartChallengeDialog -> _uiState.update {
                it.copy(showStartChallengeDialog = true)
            }
            ProfileIntent.HideStartChallengeDialog -> _uiState.update {
                it.copy(showStartChallengeDialog = false)
            }
            is ProfileIntent.ShowAchievementDetails -> _uiState.update {
                it.copy(showAchievementDetails = intent.achievement)
            }
            ProfileIntent.HideAchievementDetails -> _uiState.update {
                it.copy(showAchievementDetails = null)
            }
            ProfileIntent.HideChallengeCompletion -> _uiState.update {
                it.copy(showChallengeCompletion = null)
            }

            ProfileIntent.ClearError -> _uiState.update { it.copy(error = null) }
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load profile data"
                    )
                }
            }.collect { data ->
                _uiState.update {
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
            _uiState.update { it.copy(isLoading = true) }

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

    private fun selectCharacter(character: CharacterType) {
        viewModelScope.launch {
            val currentProfile = _uiState.value.profile

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
                        _uiState.update { it.copy(showChallengeCompletion = result) }

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
                        val currentProfile = _uiState.value.profile
                        if (result.challenge.xpReward > 0) {
                            _effects.trySend(ProfileEffect.LevelUp)
                        }
                    }
            }
        }
    }

    private data class ProfileData(
        val profile: UserProfile,
        val activeChallenges: List<Challenge>,
        val achievements: List<Achievement>,
        val completedChallenges: List<Challenge>
    )
}
