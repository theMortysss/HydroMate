package sdf.bitt.hydromate.data.mappers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import sdf.bitt.hydromate.data.local.entities.UserSettingsEntity
import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.UserSettings
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object UserSettingsMapper {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val json = Json { ignoreUnknownKeys = true }

    fun toDomain(entity: UserSettingsEntity?): UserSettings {
        return entity?.let {
            UserSettings(
                dailyGoal = it.dailyGoal,
                selectedCharacter = CharacterType.valueOf(it.selectedCharacter),
                notificationsEnabled = it.notificationsEnabled,
                notificationInterval = it.notificationInterval,
                wakeUpTime = LocalTime.parse(it.wakeUpTime, timeFormatter),
                bedTime = LocalTime.parse(it.bedTime, timeFormatter),
                quickAmounts = try {
                    json.decodeFromString<List<Int>>(it.quickAmounts)
                } catch (e: Exception) {
                    listOf(250, 500, 750)
                }
            )
        } ?: UserSettings() // Default settings if entity is null
    }

    fun toEntity(domain: UserSettings): UserSettingsEntity {
        return UserSettingsEntity(
            id = 1,
            dailyGoal = domain.dailyGoal,
            selectedCharacter = domain.selectedCharacter.name,
            notificationsEnabled = domain.notificationsEnabled,
            notificationInterval = domain.notificationInterval,
            wakeUpTime = domain.wakeUpTime.format(timeFormatter),
            bedTime = domain.bedTime.format(timeFormatter),
            quickAmounts = json.encodeToString(domain.quickAmounts)
        )
    }
}
