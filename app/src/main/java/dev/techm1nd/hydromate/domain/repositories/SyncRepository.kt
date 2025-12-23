package dev.techm1nd.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.SyncStatus
import java.time.LocalDateTime

/**
 * Репозиторий для синхронизации данных с сервером
 * Принцип offline-first: локальная база данных - источник истины
 */
interface SyncRepository {
    /**
     * Наблюдать за статусом синхронизации
     */
    fun observeSyncStatus(): Flow<SyncStatus>

    /**
     * Получить время последней синхронизации
     */
    suspend fun getLastSyncTime(): LocalDateTime?

    /**
     * Синхронизировать все данные пользователя
     */
    suspend fun syncAll(): Result<Unit>

    /**
     * Синхронизировать записи о воде
     */
    suspend fun syncWaterEntries(): Result<Unit>

    /**
     * Синхронизировать настройки пользователя
     */
    suspend fun syncUserSettings(): Result<Unit>

    /**
     * Синхронизировать профиль пользователя
     */
    suspend fun syncUserProfile(): Result<Unit>

    /**
     * Синхронизировать челленджи
     */
    suspend fun syncChallenges(): Result<Unit>

    /**
     * Синхронизировать достижения
     */
    suspend fun syncAchievements(): Result<Unit>

    /**
     * Загрузить данные с сервера (первый вход)
     */
    suspend fun downloadAllData(): Result<Unit>

    /**
     * Загрузить данные на сервер (backup)
     */
    suspend fun uploadAllData(): Result<Unit>

    /**
     * Очистить локальные данные (при выходе)
     */
    suspend fun clearLocalData(): Result<Unit>
}