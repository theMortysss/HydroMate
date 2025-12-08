package sdf.bitt.hydromate.data.mappers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import sdf.bitt.hydromate.data.local.entities.UserSettingsEntity
import sdf.bitt.hydromate.domain.entities.*
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
            val profile = UserProfile(
                gender = Gender.fromString(it.profileGender),
                weightKg = it.profileWeightKg,
                activityLevel = ActivityLevel.fromString(it.profileActivityLevel),
                climate = Climate.fromString(it.profileClimate),
                isManualGoal = it.isManualGoal,
                manualGoal = it.manualGoal
            )

            UserSettings(
                dailyGoal = it.dailyGoal,
                selectedCharacter = CharacterType.valueOf(it.selectedCharacter),
                notificationsEnabled = it.notificationsEnabled,
                notificationInterval = it.notificationInterval,
                wakeUpTime = LocalTime.parse(it.wakeUpTime, timeFormatter),
                bedTime = LocalTime.parse(it.bedTime, timeFormatter),
                quickAddPresets = parseQuickAddPresets(it.quickAddPresets),
                showNetHydration = it.showNetHydration,
                profile = profile
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
            quickAmounts = json.encodeToString(domain.quickAddPresets.map { it.amount }),
            showNetHydration = domain.showNetHydration,
            profileGender = domain.profile.gender.name,
            profileWeightKg = domain.profile.weightKg,
            profileActivityLevel = domain.profile.activityLevel.name,
            profileClimate = domain.profile.climate.name,
            isManualGoal = domain.profile.isManualGoal,
            manualGoal = domain.profile.manualGoal
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
            try {
                val amounts = json.decodeFromString<List<Int>>(jsonString)
                amounts.mapIndexed { index, amount ->
                    QuickAddPreset(
                        amount = amount,
                        drinkId = 1,
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