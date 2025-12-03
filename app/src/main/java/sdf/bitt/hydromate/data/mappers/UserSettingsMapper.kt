package sdf.bitt.hydromate.data.mappers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import sdf.bitt.hydromate.data.local.entities.UserSettingsEntity
import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.QuickAddPreset
import sdf.bitt.hydromate.domain.entities.UserSettings
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object UserSettingsMapper {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun toDomain(entity: UserSettingsEntity?): UserSettings {
        return entity?.let {
            UserSettings(
                dailyGoal = it.dailyGoal,
                selectedCharacter = CharacterType.valueOf(it.selectedCharacter),
                notificationsEnabled = it.notificationsEnabled,
                notificationInterval = it.notificationInterval,
                wakeUpTime = LocalTime.parse(it.wakeUpTime, timeFormatter),
                bedTime = LocalTime.parse(it.bedTime, timeFormatter),
                quickAddPresets = parseQuickAddPresets(it.quickAddPresets),
                showNetHydration = it.showNetHydration
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
            quickAddPresets = json.encodeToString(domain.quickAddPresets),
            quickAmounts = json.encodeToString(domain.quickAddPresets.map { it.amount }), // Для обратной совместимости
            showNetHydration = domain.showNetHydration
        )
    }

    private fun parseQuickAddPresets(jsonString: String): List<QuickAddPreset> {
        return try {
            if (jsonString.isBlank() || jsonString == "[]") {
                QuickAddPreset.getDefaults()
            } else {
                json.decodeFromString<List<QuickAddPreset>>(jsonString)
            }
        } catch (e: Exception) {
            // Fallback: пытаемся парсить как старый формат (List<Int>)
            try {
                val amounts = json.decodeFromString<List<Int>>(jsonString)
                amounts.mapIndexed { index, amount ->
                    QuickAddPreset(
                        amount = amount,
                        drinkId = 1, // Вода по умолчанию
                        drinkName = "Water",
                        drinkIcon = "💧",
                        order = index
                    )
                }
            } catch (e2: Exception) {
                QuickAddPreset.getDefaults()
            }
        }
    }
}