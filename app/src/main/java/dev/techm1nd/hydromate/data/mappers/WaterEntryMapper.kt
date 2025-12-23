package dev.techm1nd.hydromate.data.mappers

import dev.techm1nd.hydromate.data.local.entities.WaterEntryEntity
import dev.techm1nd.hydromate.domain.entities.DrinkType
import dev.techm1nd.hydromate.domain.entities.WaterEntry
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object WaterEntryMapper {

    fun toDomain(entity: WaterEntryEntity): WaterEntry {
        return WaterEntry(
            id = entity.id,
            amount = entity.amount,
            timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(entity.timestamp),
                ZoneId.systemDefault()
            ),
            drinkId = entity.drinkId,
            type = try {
                DrinkType.valueOf(entity.drinkType)
            } catch (e: Exception) {
                DrinkType.WATER // Default fallback
            }
        )
    }

    fun toEntity(domain: WaterEntry): WaterEntryEntity {
        return WaterEntryEntity(
            id = domain.id,
            amount = domain.amount,
            timestamp = domain.timestamp.atZone(ZoneId.systemDefault()).toEpochSecond(),
            drinkType = domain.type.name, // Deprecated но сохраняем
            drinkId = domain.drinkId
        )
    }

    fun toDomainList(entities: List<WaterEntryEntity>): List<WaterEntry> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<WaterEntry>): List<WaterEntryEntity> {
        return domains.map { toEntity(it) }
    }
}