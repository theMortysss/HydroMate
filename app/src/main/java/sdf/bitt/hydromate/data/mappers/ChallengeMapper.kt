package sdf.bitt.hydromate.data.mappers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import sdf.bitt.hydromate.data.local.entities.ChallengeEntity
import sdf.bitt.hydromate.domain.entities.Challenge
import sdf.bitt.hydromate.domain.entities.ChallengeType
import sdf.bitt.hydromate.domain.entities.ChallengeViolation
import java.time.LocalDate

object ChallengeMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun toDomain(entity: ChallengeEntity): Challenge {
        return Challenge(
            id = entity.id,
            type = ChallengeType.valueOf(entity.type),
            durationDays = entity.durationDays,
            startDate = LocalDate.ofEpochDay(entity.startDate),
            endDate = LocalDate.ofEpochDay(entity.endDate),
            isActive = entity.isActive,
            isCompleted = entity.isCompleted,
            currentStreak = entity.currentStreak,
            violations = parseViolations(entity.violations)
        )
    }

    fun toEntity(domain: Challenge): ChallengeEntity {
        return ChallengeEntity(
            id = domain.id,
            type = domain.type.name,
            durationDays = domain.durationDays,
            startDate = domain.startDate.toEpochDay(),
            endDate = domain.endDate.toEpochDay(),
            isActive = domain.isActive,
            isCompleted = domain.isCompleted,
            currentStreak = domain.currentStreak,
            violations = serializeViolations(domain.violations)
        )
    }

    private fun parseViolations(jsonString: String): List<ChallengeViolation> {
        return try {
            if (jsonString.isBlank() || jsonString == "[]") {
                emptyList()
            } else {
                json.decodeFromString<List<ChallengeViolation>>(jsonString)
            }
        } catch (e: Exception) {
            android.util.Log.e("ChallengeMapper", "Failed to parse violations", e)
            emptyList()
        }
    }

    private fun serializeViolations(violations: List<ChallengeViolation>): String {
        return try {
            json.encodeToString(violations)
        } catch (e: Exception) {
            android.util.Log.e("ChallengeMapper", "Failed to serialize violations", e)
            "[]"
        }
    }
}
