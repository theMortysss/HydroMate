package sdf.bitt.hydromate.data.mappers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import sdf.bitt.hydromate.data.local.entities.UserProfileEntity
import sdf.bitt.hydromate.domain.entities.CharacterType
import sdf.bitt.hydromate.domain.entities.UserProfile

object UserProfileMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun toDomain(entity: UserProfileEntity?): UserProfile {
        return entity?.let {
            UserProfile(
                level = it.level,
                currentXP = it.currentXP,
                totalXP = it.totalXP,
                selectedCharacter = try {
                    CharacterType.valueOf(it.selectedCharacter)
                } catch (e: Exception) {
                    CharacterType.PENGUIN
                },
                unlockedCharacters = parseUnlockedCharacters(it.unlockedCharacters),
                totalDrinksDrank = it.totalDrinksDrank,
                uniqueDrinksTried = parseUniqueDrinks(it.uniqueDrinksTried),
                challengesCompleted = it.challengesCompleted,
                achievementsUnlocked = it.achievementsUnlocked
            )
        } ?: UserProfile()
    }

    fun toEntity(domain: UserProfile): UserProfileEntity {
        return UserProfileEntity(
            id = 1,
            level = domain.level,
            currentXP = domain.currentXP,
            totalXP = domain.totalXP,
            selectedCharacter = domain.selectedCharacter.name,
            unlockedCharacters = json.encodeToString(
                domain.unlockedCharacters.map { it.name }
            ),
            totalDrinksDrank = domain.totalDrinksDrank,
            uniqueDrinksTried = json.encodeToString(domain.uniqueDrinksTried.toList()),
            challengesCompleted = domain.challengesCompleted,
            achievementsUnlocked = domain.achievementsUnlocked
        )
    }

    private fun parseUnlockedCharacters(jsonString: String): Set<CharacterType> {
        return try {
            val names = json.decodeFromString<List<String>>(jsonString)
            names.mapNotNull {
                try { CharacterType.valueOf(it) } catch (e: Exception) { null }
            }.toSet()
        } catch (e: Exception) {
            setOf(CharacterType.PENGUIN)
        }
    }

    private fun parseUniqueDrinks(jsonString: String): Set<String> {
        return try {
            json.decodeFromString<List<String>>(jsonString).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}
