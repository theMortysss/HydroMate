package sdf.bitt.hydromate.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.entities.DrinkType
import sdf.bitt.hydromate.domain.usecases.AddWaterEntryUseCase
import sdf.bitt.hydromate.domain.usecases.CalculateCharacterStateUseCase
import sdf.bitt.hydromate.domain.usecases.DeleteWaterEntryUseCase
import sdf.bitt.hydromate.domain.usecases.GetTodayProgressUseCase
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val addWaterEntryUseCase: AddWaterEntryUseCase,
    private val getTodayProgressUseCase: GetTodayProgressUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val deleteWaterEntryUseCase: DeleteWaterEntryUseCase,
    private val calculateCharacterStateUseCase: CalculateCharacterStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    init {
        observeData()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.AddWater -> addWater(intent.amount, intent.type)
            is HomeIntent.DeleteEntry -> deleteEntry(intent.entryId)
            HomeIntent.RefreshData -> refreshData()
            HomeIntent.ClearError -> clearError()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                getTodayProgressUseCase(),
                getUserSettingsUseCase()
            ) { progress, settings ->
                val characterState = calculateCharacterStateUseCase(progress)

                HomeUiState(
                    todayProgress = progress,
                    userSettings = settings,
                    characterState = characterState,
                    isLoading = false
                )
            }.catch { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            }.collect { newState ->
                val previousGoalReached = _uiState.value.todayProgress?.isGoalReached == true
                val currentGoalReached = newState.todayProgress?.isGoalReached == true

                _uiState.update { newState }

                // Show celebration if goal was just reached
                if (!previousGoalReached && currentGoalReached) {
                    _effects.trySend(HomeEffect.ShowGoalReachedCelebration)
                }
            }
        }
    }

    private fun addWater(amount: Int, type: DrinkType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingWater = true) }

            addWaterEntryUseCase(amount, type)
                .onSuccess {
                    _effects.trySend(HomeEffect.ShowAddWaterAnimation)
                    _effects.trySend(HomeEffect.HapticFeedback)
                }
                .onFailure { exception ->
                    _effects.trySend(
                        HomeEffect.ShowError(
                            exception.message ?: "Failed to add water entry"
                        )
                    )
                }

            _uiState.update { it.copy(isAddingWater = false) }
        }
    }

    private fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            deleteWaterEntryUseCase(entryId)
                .onFailure { exception ->
                    _effects.trySend(
                        HomeEffect.ShowError(
                            exception.message ?: "Failed to delete entry"
                        )
                    )
                }
        }
    }

    private fun refreshData() {
        // Data is automatically refreshed through Flow observation
        // This method can be used for manual refresh if needed
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}