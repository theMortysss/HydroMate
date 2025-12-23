package dev.techm1nd.hydromate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import dev.techm1nd.hydromate.domain.entities.AuthResult
import dev.techm1nd.hydromate.domain.entities.AuthState
import dev.techm1nd.hydromate.domain.entities.User
import dev.techm1nd.hydromate.domain.repositories.AuthRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val authState = if (firebaseUser != null) {
                AuthState.Authenticated(firebaseUser.toUser())
            } else {
                AuthState.Unauthenticated
            }
            trySend(authState)
        }

        firebaseAuth.addAuthStateListener(listener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toUser()
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user?.toUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Failed to get user data")
            }
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(e.localizedMessage ?: "Authentication failed", e)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Unknown error occurred", e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Обновляем displayName если указан
                if (displayName != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    firebaseUser.updateProfile(profileUpdates).await()
                }

                AuthResult.Success(firebaseUser.toUser())
            } else {
                AuthResult.Error("Failed to create user")
            }
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(e.localizedMessage ?: "Sign up failed", e)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Unknown error occurred", e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user?.toUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Failed to get user data")
            }
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(e.localizedMessage ?: "Google sign in failed", e)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Unknown error occurred", e)
        }
    }

    override suspend fun signInAnonymously(): AuthResult {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            val user = result.user?.toUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Failed to create anonymous user")
            }
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(e.localizedMessage ?: "Anonymous sign in failed", e)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Unknown error occurred", e)
        }
    }

    // ... (существующий код)

    override suspend fun linkAnonymousWithEmail(email: String, password: String): AuthResult {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null || !currentUser.isAnonymous) {
                return AuthResult.Error("No anonymous user to link")
            }
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            val result = currentUser.linkWithCredential(credential).await()
            val user = result.user?.toUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Failed to link account")
            }
        } catch (e: FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use. Try signing in instead."
                else -> e.localizedMessage ?: "Failed to link account"
            }
            AuthResult.Error(message, e)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Unknown error occurred", e)
        }
    }

    override suspend fun linkAnonymousWithGoogle(idToken: String): AuthResult {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null || !currentUser.isAnonymous) {
                return AuthResult.Error("No anonymous user to link")
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = currentUser.linkWithCredential(credential).await()
            val user = result.user?.toUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Failed to link account")
            }
        } catch (e: FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_GOOGLE_ALREADY_IN_USE" -> "Google already in use. Try signing in instead."
                else -> e.localizedMessage ?: "Failed to link account"
            }
            AuthResult.Error(message, e)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Unknown error occurred", e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            if (e.errorCode == "ERROR_REQUIRES_RECENT_LOGIN") {
                Result.failure(Exception("Please sign in again to delete account"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Конвертация FirebaseUser в domain User
     */
    private fun FirebaseUser.toUser(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isAnonymous = isAnonymous,
            createdAt = metadata?.creationTimestamp?.let {
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it),
                    ZoneId.systemDefault()
                )
            } ?: LocalDateTime.now()
        )
    }
}