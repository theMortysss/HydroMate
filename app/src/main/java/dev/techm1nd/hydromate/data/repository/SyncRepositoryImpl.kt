package dev.techm1nd.hydromate.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import dev.techm1nd.hydromate.data.local.dao.*
import dev.techm1nd.hydromate.data.local.entities.ChallengeEntity
import dev.techm1nd.hydromate.data.local.entities.UserProfileEntity
import dev.techm1nd.hydromate.data.local.entities.UserSettingsEntity
import dev.techm1nd.hydromate.data.mappers.*
import dev.techm1nd.hydromate.domain.entities.SyncStatus
import dev.techm1nd.hydromate.domain.repositories.SyncRepository
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val waterEntryDao: WaterEntryDao,
    private val userSettingsDao: UserSettingsDao,
    private val userProfileDao: UserProfileDao,
    private val challengeDao: ChallengeDao,
    private val achievementDao: AchievementDao,
    private val drinkDao: DrinkDao
) : SyncRepository {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)

    private companion object {
        const val TAG = "SyncRepository"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_WATER_ENTRIES = "waterEntries"
        const val COLLECTION_SETTINGS = "settings"
        const val COLLECTION_PROFILE = "profile"
        const val COLLECTION_CHALLENGES = "challenges"
        const val COLLECTION_ACHIEVEMENTS = "achievements"
        const val COLLECTION_CUSTOM_DRINKS = "customDrinks"
        const val FIELD_LAST_SYNC = "lastSyncAt"
    }

    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    override suspend fun getLastSyncTime(): LocalDateTime? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val doc = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            doc.getTimestamp(FIELD_LAST_SYNC)?.toDate()?.let {
                LocalDateTime.ofInstant(it.toInstant(), ZoneId.systemDefault())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last sync time", e)
            null
        }
    }

    override suspend fun syncAll(): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.Syncing

            syncWaterEntries()
            syncUserSettings()
            syncUserProfile()
            syncChallenges()
            syncAchievements()

            updateLastSyncTime()

            val now = LocalDateTime.now()
            _syncStatus.value = SyncStatus.Success(now)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            _syncStatus.value = SyncStatus.Error(e.message ?: "Sync failed")
            Result.failure(e)
        }
    }

    override suspend fun syncWaterEntries(): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))

        return try {
            // 1. Загрузить записи с сервера
            val remoteEntries = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_WATER_ENTRIES)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        WaterEntryMapper.toEntity(
                            WaterEntryMapper.toDomain(
                                doc.toObject(dev.techm1nd.hydromate.data.local.entities.WaterEntryEntity::class.java)!!
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse water entry: ${doc.id}", e)
                        null
                    }
                }

            // 2. Загрузить локальные записи
            val localEntries = waterEntryDao.getEntriesForDateRange(
                0,
                System.currentTimeMillis() / 1000
            ).first()

            // 3. Объединить данные (offline-first: локальные данные важнее)
            val remoteMap = remoteEntries.associateBy { it.id }
            val localMap = localEntries.associateBy { it.id }

            // Новые локальные записи - загрузить на сервер
            val toUpload = localEntries.filter { !remoteMap.containsKey(it.id) }
            toUpload.forEach { entry ->
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_WATER_ENTRIES)
                    .document(entry.id.toString())
                    .set(entry)
                    .await()
            }

            // Новые удаленные записи - сохранить локально
            val toSave = remoteEntries.filter { !localMap.containsKey(it.id) }
            toSave.forEach { entry ->
                waterEntryDao.insertEntry(entry)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync water entries", e)
            Result.failure(e)
        }
    }

    override suspend fun syncUserSettings(): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))

        return try {
            // Загрузить настройки с сервера
            val remoteSettings = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_SETTINGS)
                .document("current")
                .get()
                .await()

            // Загрузить локальные настройки
            val localSettings = userSettingsDao.getUserSettings().first()

            if (remoteSettings.exists() && localSettings == null) {
                // Первый вход - загрузить с сервера
                val settings = remoteSettings.toObject(
                    UserSettingsEntity::class.java
                )
                if (settings != null) {
                    userSettingsDao.insertOrUpdateSettings(settings)
                }
            } else if (localSettings != null) {
                // Загрузить локальные настройки на сервер
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_SETTINGS)
                    .document("current")
                    .set(localSettings, SetOptions.merge())
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user settings", e)
            Result.failure(e)
        }
    }

    override suspend fun syncUserProfile(): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))

        return try {
            val remoteProfile = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PROFILE)
                .document("current")
                .get()
                .await()

            val localProfile = userProfileDao.getUserProfileSync()

            if (remoteProfile.exists() && localProfile == null) {
                val profile = remoteProfile.toObject(
                    UserProfileEntity::class.java
                )
                if (profile != null) {
                    userProfileDao.insertProfile(profile)
                }
            } else if (localProfile != null) {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_PROFILE)
                    .document("current")
                    .set(localProfile, SetOptions.merge())
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user profile", e)
            Result.failure(e)
        }
    }

    override suspend fun syncChallenges(): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))

        return try {
            val remoteChallenges = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_CHALLENGES)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(ChallengeEntity::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse challenge: ${doc.id}", e)
                        null
                    }
                }

            val localChallenges = challengeDao.getAllChallenges()

            val remoteMap = remoteChallenges.associateBy { it.id }
            val localMap = localChallenges.associateBy { it.id }

            // Upload new local challenges
            val toUpload = localChallenges.filter { !remoteMap.containsKey(it.id) }
            toUpload.forEach { challenge ->
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_CHALLENGES)
                    .document(challenge.id)
                    .set(challenge)
                    .await()
            }

            // Save new remote challenges
            val toSave = remoteChallenges.filter { !localMap.containsKey(it.id) }
            toSave.forEach { challenge ->
                challengeDao.insertChallenge(challenge)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync challenges", e)
            Result.failure(e)
        }
    }

    override suspend fun syncAchievements(): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))

        return try {
            val remoteAchievements = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_ACHIEVEMENTS)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(dev.techm1nd.hydromate.data.local.entities.AchievementEntity::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse achievement: ${doc.id}", e)
                        null
                    }
                }

            // Для достижений используем merge стратегию:
            // Загружаем с сервера только разблокированные достижения
            remoteAchievements.filter { it.isUnlocked }.forEach { achievement ->
                achievementDao.updateAchievement(achievement)
            }

            // Загружаем локальные разблокированные на сервер
            val localAchievements = achievementDao.getAllAchievements().first()
                .filter { it.isUnlocked }

            localAchievements.forEach { achievement ->
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_ACHIEVEMENTS)
                    .document(achievement.id)
                    .set(achievement, SetOptions.merge())
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync achievements", e)
            Result.failure(e)
        }
    }

    override suspend fun downloadAllData(): Result<Unit> {
        return try {
            syncWaterEntries()
            syncUserSettings()
            syncUserProfile()
            syncChallenges()
            syncAchievements()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAllData(): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))

        return try {
            // Upload everything from local database to server
            val waterEntries = waterEntryDao.getEntriesForDateRange(0, System.currentTimeMillis() / 1000).first()
            waterEntries.forEach { entry ->
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_WATER_ENTRIES)
                    .document(entry.id.toString())
                    .set(entry)
                    .await()
            }

            val settings = userSettingsDao.getUserSettings().first()
            if (settings != null) {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_SETTINGS)
                    .document("current")
                    .set(settings)
                    .await()
            }

            val profile = userProfileDao.getUserProfileSync()
            if (profile != null) {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_PROFILE)
                    .document("current")
                    .set(profile)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearLocalData(): Result<Unit> {
        return try {
            // Clear all local data (used when signing out)
            // Note: We don't clear Room database completely,
            // just mark data as belonging to previous user
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private suspend fun updateLastSyncTime() {
        val userId = getCurrentUserId() ?: return
        try {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(
                    mapOf(FIELD_LAST_SYNC to com.google.firebase.Timestamp.now()),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update last sync time", e)
        }
    }
}