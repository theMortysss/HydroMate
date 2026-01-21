package dev.techm1nd.hydromate.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.data.worker.SyncWorker
import dev.techm1nd.hydromate.domain.entities.AuthResult
import dev.techm1nd.hydromate.domain.entities.AuthState
import dev.techm1nd.hydromate.domain.usecases.auth.*
import dev.techm1nd.hydromate.ui.screens.auth.model.AuthEffect
import dev.techm1nd.hydromate.ui.screens.auth.model.AuthIntent
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase,
    private val linkAnonymousWithEmailUseCase: LinkAnonymousWithEmailUseCase,
    private val linkAnonymousWithGoogleUseCase: LinkAnonymousWithGoogleUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(dev.techm1nd.hydromate.ui.screens.auth.model.AuthState())
    val state: StateFlow<dev.techm1nd.hydromate.ui.screens.auth.model.AuthState> = _state.asStateFlow()

    private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
    val effects: Flow<AuthEffect> = _effects.receiveAsFlow()

    init {
        observeAuthState()
    }

    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.SignInWithEmail -> signInWithEmail(intent.email, intent.password)
            is AuthIntent.SignUpWithEmail -> signUpWithEmail(
                intent.email,
                intent.password,
                intent.displayName
            )
            is AuthIntent.SignInWithGoogle -> signInWithGoogle(intent.idToken)
            AuthIntent.SignInAnonymously -> signInAnonymously()
            is AuthIntent.LinkWithEmail -> linkWithEmail(intent.email, intent.password)
            is AuthIntent.LinkWithGoogle -> linkWithGoogle(intent.idToken)
            is AuthIntent.ResetPassword -> resetPassword(intent.email)
            AuthIntent.SignOut -> signOut()

            AuthIntent.ShowEmailSignIn -> _state.update {
                it.copy(showEmailSignIn = true, showEmailSignUp = false, showLinkAccount = false)
            }
            AuthIntent.ShowEmailSignUp -> _state.update {
                it.copy(showEmailSignUp = true, showEmailSignIn = false, showLinkAccount = false)
            }
            AuthIntent.ShowLinkAccount -> _state.update {
                it.copy(showLinkAccount = true, showEmailSignIn = false, showEmailSignUp = false)
            }
            AuthIntent.HideDialogs -> _state.update {
                it.copy(
                    showEmailSignIn = false,
                    showEmailSignUp = false,
                    showLinkAccount = false
                )
            }
            AuthIntent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase()
                .collect { authState ->
                    when (authState) {
                        is AuthState.Authenticated -> {
                            _state.update {
                                it.copy(
                                    currentUser = authState.user,
                                    isAnonymous = authState.user.isAnonymous,
                                    isLoading = false
                                )
                            }
                            _effects.trySend(AuthEffect.NavigateToHome)
                        }
                        AuthState.Unauthenticated -> {
                            _state.update {
                                it.copy(
                                    currentUser = null,
                                    isAnonymous = false,
                                    isLoading = false
                                )
                            }
                        }
                        AuthState.Loading -> {
                            _state.update { it.copy(isLoading = true) }
                        }
                    }
                }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = signInWithEmailUseCase(email, password)) {
                is AuthResult.Success -> {
                    _effects.trySend(AuthEffect.ShowSuccess("Welcome back!"))
                    _state.update { it.copy(showEmailSignIn = false, isLoading = false) }

                    // Schedule sync worker for registered user
                    if (!result.user.isAnonymous) {
                        SyncWorker.schedule(context)
                    }
                    // NavigateToHome will be triggered by observeAuthState
                }
                is AuthResult.Error -> {
                    _state.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                    _effects.trySend(AuthEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun signUpWithEmail(email: String, password: String, displayName: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = signUpWithEmailUseCase(email, password, displayName)) {
                is AuthResult.Success -> {
                    _effects.trySend(AuthEffect.ShowSuccess("Account created successfully!"))
                    _state.update { it.copy(showEmailSignUp = false) }

                    // Schedule sync worker for registered user
                    if (!result.user.isAnonymous) {
                        SyncWorker.schedule(context)
                    }
                    // NavigateToHome will be triggered by observeAuthState
                }
                is AuthResult.Error -> {
                    _state.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                    _effects.trySend(AuthEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = signInWithGoogleUseCase(idToken)) {
                is AuthResult.Success -> {
                    _effects.trySend(AuthEffect.ShowSuccess("Welcome!"))

                    // Schedule sync worker for registered user
                    if (!result.user.isAnonymous) {
                        SyncWorker.schedule(context)
                    }
                    // NavigateToHome will be triggered by observeAuthState
                }
                is AuthResult.Error -> {
                    _state.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                    _effects.trySend(AuthEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun signInAnonymously() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = signInAnonymouslyUseCase()) {
                is AuthResult.Success -> {
                    _effects.trySend(
                        AuthEffect.ShowSuccess(
                            "Started anonymously. Link your account later to save your progress!"
                        )
                    )

                    // Cancel any existing sync worker for anonymous user
                    SyncWorker.cancel(context)
                    // NavigateToHome will be triggered by observeAuthState
                }
                is AuthResult.Error -> {
                    _state.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                    _effects.trySend(AuthEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun linkWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = linkAnonymousWithEmailUseCase(email, password)) {
                is AuthResult.Success -> {
                    _effects.trySend(
                        AuthEffect.ShowSuccess("Account linked! Your progress is now saved.")
                    )
                    _state.update { it.copy(showLinkAccount = false, isAnonymous = false) }
                }
                is AuthResult.Error -> {
                    _state.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                    _effects.trySend(AuthEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun linkWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = linkAnonymousWithGoogleUseCase(idToken)) {
                is AuthResult.Success -> {
                    _effects.trySend(
                        AuthEffect.ShowSuccess("Account linked! Your progress is now saved.")
                    )
                    _state.update { it.copy(showLinkAccount = false, isAnonymous = false) }
                }
                is AuthResult.Error -> {
                    _state.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                    _effects.trySend(AuthEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun resetPassword(email: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            resetPasswordUseCase(email)
                .onSuccess {
                    _effects.trySend(
                        AuthEffect.ShowSuccess("Password reset email sent! Check your inbox.")
                    )
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            error = exception.message,
                            isLoading = false
                        )
                    }
                    _effects.trySend(
                        AuthEffect.ShowError(exception.message ?: "Failed to send reset email")
                    )
                }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            signOutUseCase()
                .onSuccess {
                    _effects.trySend(AuthEffect.ShowSuccess("Signed out successfully"))
                }
                .onFailure { exception ->
                    _effects.trySend(
                        AuthEffect.ShowError(exception.message ?: "Failed to sign out")
                    )
                }

            _state.update { it.copy(isLoading = false) }
        }
    }
}