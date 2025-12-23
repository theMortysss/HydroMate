package dev.techm1nd.hydromate.domain.usecases.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dev.techm1nd.hydromate.domain.repositories.AuthRepository
import dev.techm1nd.hydromate.domain.repositories.SyncRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UpdateAuthProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(newDisplayName: String): Result<Unit> {
        return try {
            val currentUser = authRepository.getCurrentUser() ?: return Result.failure(Exception("No user"))
            val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return Result.failure(Exception("No Firebase user"))

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            syncRepository.syncUserProfile() // Sync в Firestore
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}