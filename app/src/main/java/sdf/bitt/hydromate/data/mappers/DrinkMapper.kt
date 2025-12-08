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
            } catch (e: IllegalArgumentException) {
                DrinkType.CUSTOM
            },
            caffeineContent = entity.caffeineContent,
            alcoholPercentage = entity.alcoholPercentage,
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
            caffeineContent = domain.caffeineContent,
            alcoholPercentage = domain.alcoholPercentage,
            isCustom = domain.isCustom,
            color = domain.color,
        )
    }

    fun toDomainList(entities: List<DrinkEntity>): List<Drink> =
        entities.map { toDomain(it) }

    fun toEntityList(drinks: List<Drink>): List<DrinkEntity> =
        drinks.map { toEntity(it) }
}