package dev.techm1nd.hydromate.domain.entities

import java.time.LocalDateTime

/**
 * Состояние аутентификации пользователя
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Loading : AuthState()
}

/**
 * Пользователь приложения
 */
data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastSyncAt: LocalDateTime? = null
) {
    /**
     * Отображаемое имя для UI
     */
    val displayNameOrEmail: String
        get() = displayName ?: email ?: "Anonymous User"
}

/**
 * Результат операции аутентификации
 */
sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String, val exception: Exception? = null) : AuthResult()
}

/**
 * Тип провайдера аутентификации
 */
enum class AuthProvider {
    EMAIL,
    GOOGLE,
    ANONYMOUS
}

/**
 * Статус синхронизации данных
 */
sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val syncedAt: LocalDateTime) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

/**
 * Конфликт при синхронизации
 */
enum class SyncConflictResolution {
    LOCAL_WINS,    // Локальные данные важнее
    REMOTE_WINS,   // Серверные данные важнее
    MERGE          // Объединить данные
}