package sdf.bitt.hydromate.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sdf.bitt.hydromate.data.local.dao.DrinkDao
import sdf.bitt.hydromate.data.mappers.DrinkMapper
import sdf.bitt.hydromate.domain.entities.Drink
import sdf.bitt.hydromate.domain.entities.DrinkType
import sdf.bitt.hydromate.domain.repositories.DrinkRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrinkRepositoryImpl @Inject constructor(
    private val drinkDao: DrinkDao
) : DrinkRepository {

    override fun getAllActiveDrinks(): Flow<List<Drink>> {
        return drinkDao.getAllActiveDrinks()
            .map { entities -> entities.map { DrinkMapper.toDomain(it) } }
    }

    override fun getDrinksByCategory(category: DrinkType): Flow<List<Drink>> {
        return drinkDao.getDrinksByCategory(category.name)
            .map { entities -> entities.map { DrinkMapper.toDomain(it) } }
    }

    override suspend fun getDrinkById(drinkId: Long): Result<Drink?> {
        return try {
            val entity = drinkDao.getDrinkById(drinkId)
            Result.success(entity?.let { DrinkMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDrinkByIdFlow(drinkId: Long): Flow<Drink?> {
        return drinkDao.getDrinkByIdFlow(drinkId)
            .map { entity -> entity?.let { DrinkMapper.toDomain(it) } }
    }

    override suspend fun createCustomDrink(drink: Drink): Result<Long> {
        return try {
            val entity = DrinkMapper.toEntity(drink.copy(isCustom = true))
            val id = drinkDao.insertDrink(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDrink(drink: Drink): Result<Unit> {
        return try {
            val entity = DrinkMapper.toEntity(drink)
            drinkDao.updateDrink(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDrink(drinkId: Long): Result<Unit> {
        return try {
            drinkDao.deactivateDrink(drinkId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCustomDrinks(): Flow<List<Drink>> {
        return drinkDao.getCustomDrinks()
            .map { entities -> entities.map { DrinkMapper.toDomain(it) } }
    }

    override suspend fun initializeDefaultDrinks(): Result<Unit> {
        return try {
            // Проверяем, есть ли уже дефолтные напитки
            val count = drinkDao.getDefaultDrinksCount()
            if (count == 0) {
                val defaultDrinks = Drink.getDefaultDrinks()
                val entities = defaultDrinks.map { DrinkMapper.toEntity(it) }
                drinkDao.insertDrinks(entities)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}