package dev.techm1nd.hydromate.data.mappers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import dev.techm1nd.hydromate.data.local.entities.UserSettingsEntity
import dev.techm1nd.hydromate.domain.entities.*
import java.time.DayOfWeek
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
                isManualGoal = it.manualGoalEnabled,
                manualGoal = it.manualGoalMl
            )

            UserSettings(
                dailyGoal = it.dailyGoal,
//                selectedCharacter = CharacterType.valueOf(it.selectedCharacter),
                notificationsEnabled = it.notificationsEnabled,
                wakeUpTime = LocalTime.parse(it.wakeUpTime, timeFormatter),
                bedTime = LocalTime.parse(it.bedTime, timeFormatter),
                smartRemindersEnabled = it.smartRemindersEnabled,
                notificationInterval = it.notificationInterval,
                reminderInterval = ReminderInterval.fromMinutes(it.reminderIntervalMinutes),
                smartReminderDays = parseDaysOfWeek(it.smartReminderDays),
                customRemindersEnabled = it.customRemindersEnabled,
                customReminders = parseCustomReminders(it.customReminders),
                snoozeEnabled = it.snoozeEnabled,
                snoozeDelay = SnoozeDelay.fromMinutes(it.snoozeDelayMinutes),
                showProgressInNotification = it.showProgressInNotification,
                quickAddPresets = parseQuickAddPresets(it.quickAddPresets),
//                showNetHydration = it.showNetHydration,
                profile = profile
            )
        } ?: UserSettings()
    }

    fun toEntity(domain: UserSettings): UserSettingsEntity {
        return UserSettingsEntity(
            id = 1,
            dailyGoal = domain.dailyGoal,
//            selectedCharacter = domain.selectedCharacter.name,
            notificationsEnabled = domain.notificationsEnabled,
            wakeUpTime = domain.wakeUpTime.format(timeFormatter),
            bedTime = domain.bedTime.format(timeFormatter),
            smartRemindersEnabled = domain.smartRemindersEnabled,
            notificationInterval = domain.notificationInterval,
            reminderIntervalMinutes = domain.reminderInterval.minutes,
            smartReminderDays = serializeDaysOfWeek(domain.smartReminderDays),
            customRemindersEnabled = domain.customRemindersEnabled,
            customReminders = json.encodeToString(domain.customReminders),
            snoozeEnabled = domain.snoozeEnabled,
            snoozeDelayMinutes = domain.snoozeDelay.minutes,
            showProgressInNotification = domain.showProgressInNotification,
            quickAddPresets = json.encodeToString(domain.quickAddPresets),
            quickAmounts = json.encodeToString(domain.quickAddPresets.map { it.amount }),
//            showNetHydration = domain.showNetHydration,
            profileGender = domain.profile.gender.name,
            profileWeightKg = domain.profile.weightKg,
            profileActivityLevel = domain.profile.activityLevel.name,
            profileClimate = domain.profile.climate.name,
            manualGoalEnabled = domain.profile.isManualGoal,
            manualGoalMl = domain.profile.manualGoal
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

    private fun parseCustomReminders(jsonString: String): List<CustomReminder> {
        return try {
            if (jsonString.isBlank() || jsonString == "[]") {
                emptyList()
            } else {
                json.decodeFromString<List<CustomReminder>>(jsonString)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseDaysOfWeek(value: String): Set<DayOfWeek> {
        return try {
            if (value.isBlank()) {
                DayOfWeek.entries.toSet()
            } else {
                value.split(",")
                    .mapNotNull {
                        try {
                            DayOfWeek.valueOf(it.trim())
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .toSet()
            }
        } catch (e: Exception) {
            DayOfWeek.entries.toSet()
        }
    }

    private fun serializeDaysOfWeek(days: Set<DayOfWeek>): String {
        return days.joinToString(",") { it.name }
    }
}