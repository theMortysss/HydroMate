package dev.techm1nd.hydromate.ui.screens.auth.model

import android.content.Intent
import dev.techm1nd.hydromate.domain.entities.User

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val showEmailSignIn: Boolean = false,
    val showEmailSignUp: Boolean = false,
    val showLinkAccount: Boolean = false,
    val isAnonymous: Boolean = false,
    val needsOnboarding: Boolean = false
)

sealed class AuthIntent {
    data class SignInWithEmail(val email: String, val password: String) : AuthIntent()
    data class SignUpWithEmail(
        val email: String,
        val password: String,
        val displayName: String?
    ) : AuthIntent()
    data class SignInWithGoogle(val idToken: String) : AuthIntent()
    object SignInAnonymously : AuthIntent()
    data class LinkWithEmail(val email: String, val password: String) : AuthIntent()
    data class LinkWithGoogle(val idToken: String) : AuthIntent()
    data class ResetPassword(val email: String) : AuthIntent()
    object SignOut : AuthIntent()

    object ShowEmailSignIn : AuthIntent()
    object ShowEmailSignUp : AuthIntent()
    object ShowLinkAccount : AuthIntent()
    object HideDialogs : AuthIntent()
    object ClearError : AuthIntent()
}

sealed class AuthEffect {
    data class ShowSuccess(val message: String) : AuthEffect()
    data class ShowError(val message: String) : AuthEffect()
    object NavigateToHome : AuthEffect()
    data class NavigateToGoogleSignIn(val signInIntent: Intent) : AuthEffect()
}