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
import java.io.IOException
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
            AuthResult.Error(getReadableErrorMessage(e), e)
        } catch (e: IOException) {
            AuthResult.Error("No internet connection. Please check your network.", e)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
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
                // Update displayName if provided
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
            AuthResult.Error(getReadableErrorMessage(e), e)
        } catch (e: IOException) {
            AuthResult.Error("No internet connection. Please check your network.", e)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
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
            AuthResult.Error(getReadableErrorMessage(e), e)
        } catch (e: IOException) {
            AuthResult.Error("No internet connection. Please check your network.", e)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
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
            AuthResult.Error(getReadableErrorMessage(e), e)
        } catch (e: IOException) {
            // Anonymous sign-in should work offline
            AuthResult.Error("Failed to start anonymous session", e)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

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
                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                else -> getReadableErrorMessage(e)
            }
            AuthResult.Error(message, e)
        } catch (e: IOException) {
            AuthResult.Error("No internet connection. Please check your network.", e)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
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
                "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "Google account already linked to another user"
                else -> getReadableErrorMessage(e)
            }
            AuthResult.Error(message, e)
        } catch (e: IOException) {
            AuthResult.Error("No internet connection. Please check your network.", e)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(IOException("No internet connection. Please check your network."))
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
        } catch (e: IOException) {
            Result.failure(IOException("No internet connection. Please check your network."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convert Firebase errors to readable messages
     */
    private fun getReadableErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthException -> when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                "ERROR_WEAK_PASSWORD" -> "Password is too weak (min 6 characters)"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use"
                "ERROR_NETWORK_REQUEST_FAILED" -> "No internet connection"
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later"
                else -> exception.localizedMessage ?: "Authentication failed"
            }
            is IOException -> "No internet connection. Please check your network."
            else -> exception.localizedMessage ?: "An error occurred"
        }
    }

    /**
     * Convert FirebaseUser to domain User
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