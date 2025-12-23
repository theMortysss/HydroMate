package dev.techm1nd.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.AuthResult
import dev.techm1nd.hydromate.domain.entities.AuthState
import dev.techm1nd.hydromate.domain.entities.User

interface AuthRepository {
    /**
     * Наблюдать за состоянием аутентификации
     */
    fun observeAuthState(): Flow<AuthState>

    /**
     * Получить текущего пользователя
     */
    suspend fun getCurrentUser(): User?

    /**
     * Войти через email/password
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult

    /**
     * Зарегистрироваться через email/password
     */
    suspend fun signUpWithEmail(email: String, password: String, displayName: String?): AuthResult

    /**
     * Войти через Google
     */
    suspend fun signInWithGoogle(idToken: String): AuthResult

    /**
     * Войти анонимно
     */
    suspend fun signInAnonymously(): AuthResult

    /**
     * Связать анонимный аккаунт с email
     */
    suspend fun linkAnonymousWithEmail(email: String, password: String): AuthResult

    /**
     * Связать анонимный аккаунт с Google
     */
    suspend fun linkAnonymousWithGoogle(idToken: String): AuthResult

    /**
     * Сбросить пароль
     */
    suspend fun resetPassword(email: String): Result<Unit>

    /**
     * Выйти из аккаунта
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Удалить аккаунт
     */
    suspend fun deleteAccount(): Result<Unit>
}