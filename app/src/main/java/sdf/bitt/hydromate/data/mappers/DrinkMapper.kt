package sdf.bitt.hydromate.data.mappers

import sdf.bitt.hydromate.data.local.entities.DrinkEntity
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.DrinkType

object DrinkMapper {

    fun toDomain(entity: DrinkEntity): Drink {
        return Drink(
            id = entity.id,
            name = entity.name,
            icon = entity.icon,
            hydrationMultiplier = entity.hydrationMultiplier,
            category = try {
                DrinkType.valueOf(entity.category)
            } catch (e: Exception) {
                DrinkType.CUSTOM
            },
            containsCaffeine = entity.containsCaffeine,
            containsAlcohol = entity.containsAlcohol,
            isCustom = entity.isCustom,
            color = entity.color
        )
    }

    fun toEntity(domain: Drink): DrinkEntity {
        return DrinkEntity(
            id = domain.id,
            name = domain.name,
            icon = domain.icon,
            hydrationMultiplier = domain.hydrationMultiplier,
            category = domain.category.name,
            containsCaffeine = domain.containsCaffeine,
            containsAlcohol = domain.containsAlcohol,
            isCustom = domain.isCustom,
            color = domain.color
        )
    }

    fun toDomainList(entities: List<DrinkEntity>): List<Drink> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<Drink>): List<DrinkEntity> {
        return domains.map { toEntity(it) }
    }
}