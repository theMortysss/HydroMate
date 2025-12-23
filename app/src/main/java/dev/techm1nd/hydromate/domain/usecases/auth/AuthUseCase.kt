package dev.techm1nd.hydromate.domain.usecases.auth

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.AuthResult
import dev.techm1nd.hydromate.domain.entities.AuthState
import dev.techm1nd.hydromate.domain.entities.User
import dev.techm1nd.hydromate.domain.repositories.AuthRepository
import dev.techm1nd.hydromate.domain.repositories.SyncRepository
import javax.inject.Inject

/**
 * Наблюдать за состоянием аутентификации
 */
class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthState> = authRepository.observeAuthState()
}

/**
 * Получить текущего пользователя
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? = authRepository.getCurrentUser()
}

/**
 * Войти через email/password
 */
class SignInWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        val result = authRepository.signInWithEmail(email, password)

        // Если вход успешен - синхронизировать данные
        if (result is AuthResult.Success) {
            syncRepository.downloadAllData()
        }

        return result
    }
}

/**
 * Зарегистрироваться через email/password
 */
class SignUpWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String?
    ): AuthResult {
        val result = authRepository.signUpWithEmail(email, password, displayName)

        // Если регистрация успешна - загрузить локальные данные на сервер
        if (result is AuthResult.Success) {
            syncRepository.uploadAllData()
        }

        return result
    }
}

/**
 * Войти через Google
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(idToken: String): AuthResult {
        val result = authRepository.signInWithGoogle(idToken)

        if (result is AuthResult.Success) {
            syncRepository.downloadAllData()
        }

        return result
    }
}

/**
 * Войти анонимно
 */
class SignInAnonymouslyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult {
        return authRepository.signInAnonymously()
    }
}

/**
 * Связать анонимный аккаунт с email
 */
class LinkAnonymousWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        val result = authRepository.linkAnonymousWithEmail(email, password)

        // Если связывание успешно - загрузить данные на сервер
        if (result is AuthResult.Success) {
            syncRepository.uploadAllData()
        }

        return result
    }
}

/**
 * Связать анонимный аккаунт с Google
 */
class LinkAnonymousWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(idToken: String): AuthResult {
        val result = authRepository.linkAnonymousWithGoogle(idToken)

        if (result is AuthResult.Success) {
            syncRepository.uploadAllData()
        }

        return result
    }
}

/**
 * Сбросить пароль
 */
class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.resetPassword(email)
    }
}

/**
 * Выйти из аккаунта
 */
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // Синхронизировать данные перед выходом
        syncRepository.syncAll()

        // Очистить локальные данные (опционально)
        // syncRepository.clearLocalData()

        return authRepository.signOut()
    }
}

/**
 * Удалить аккаунт
 */
class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // Очистить локальные данные
        syncRepository.clearLocalData()

        return authRepository.deleteAccount()
    }
}