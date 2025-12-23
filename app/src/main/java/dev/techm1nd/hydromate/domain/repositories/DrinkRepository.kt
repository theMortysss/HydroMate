package dev.techm1nd.hydromate.domain.repositories

import kotlinx.coroutines.flow.Flow
import dev.techm1nd.hydromate.domain.entities.Drink
import dev.techm1nd.hydromate.domain.entities.DrinkType

interface DrinkRepository {

    /**
     * Получить все активные напитки
     */
    fun getAllActiveDrinks(): Flow<List<Drink>>

    /**
     * Получить напитки по категории
     */
    fun getDrinksByCategory(category: DrinkType): Flow<List<Drink>>

    /**
     * Получить напиток по ID
     */
    suspend fun getDrinkById(drinkId: Long): Result<Drink?>

    /**
     * Получить напиток по ID как Flow
     */
    fun getDrinkByIdFlow(drinkId: Long): Flow<Drink?>

    /**
     * Создать кастомный напиток
     */
    suspend fun createCustomDrink(drink: Drink): Result<Long>

    /**
     * Обновить напиток
     */
    suspend fun updateDrink(drink: Drink): Result<Unit>

    /**
     * Удалить напиток (soft delete)
     */
    suspend fun deleteDrink(drinkId: Long): Result<Unit>

    /**
     * Получить только кастомные напитки
     */
    fun getCustomDrinks(): Flow<List<Drink>>

    /**
     * Инициализировать базу данных дефолтными напитками
     */
    suspend fun initializeDefaultDrinks(): Result<Unit>
}