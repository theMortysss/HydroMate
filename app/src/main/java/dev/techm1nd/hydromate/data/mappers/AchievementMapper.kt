package dev.techm1nd.hydromate.data.mappers

import dev.techm1nd.hydromate.data.local.entities.AchievementEntity
import dev.techm1nd.hydromate.domain.entities.Achievement
import dev.techm1nd.hydromate.domain.entities.AchievementType
import dev.techm1nd.hydromate.domain.entities.CharacterType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object AchievementMapper {

    fun toDomain(entity: AchievementEntity): Achievement {
        return Achievement(
            id = entity.id,
            type = AchievementType.valueOf(entity.type),
            title = entity.title,
            description = entity.description,
            icon = entity.icon,
            xpReward = entity.xpReward,
            isUnlocked = entity.isUnlocked,
            unlockedAt = entity.unlockedAt?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            },
            progress = entity.progress,
            progressMax = entity.progressMax,
            unlockableCharacter = entity.unlockableCharacter?.let {
                try { CharacterType.valueOf(it) } catch (e: Exception) { null }
            }
        )
    }

    fun toEntity(domain: Achievement): AchievementEntity {
        return AchievementEntity(
            id = domain.id,
            type = domain.type.name,
            title = domain.title,
            description = domain.description,
            icon = domain.icon,
            xpReward = domain.xpReward,
            isUnlocked = domain.isUnlocked,
            unlockedAt = domain.unlockedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            progress = domain.progress,
            progressMax = domain.progressMax,
            unlockableCharacter = domain.unlockableCharacter?.name
        )
    }
}
