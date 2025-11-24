package sdf.bitt.hydromate.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import sdf.bitt.hydromate.data.local.entities.DrinkEntity

@Dao
interface DrinkDao {

    @Query("SELECT * FROM drinks WHERE is_active = 1 ORDER BY is_custom ASC, name ASC")
    fun getAllActiveDrinks(): Flow<List<DrinkEntity>>

    @Query("SELECT * FROM drinks WHERE category = :category AND is_active = 1 ORDER BY name ASC")
    fun getDrinksByCategory(category: String): Flow<List<DrinkEntity>>

    @Query("SELECT * FROM drinks WHERE id = :drinkId")
    suspend fun getDrinkById(drinkId: Long): DrinkEntity?

    @Query("SELECT * FROM drinks WHERE id = :drinkId")
    fun getDrinkByIdFlow(drinkId: Long): Flow<DrinkEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrink(drink: DrinkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinks(drinks: List<DrinkEntity>)

    @Update
    suspend fun updateDrink(drink: DrinkEntity)

    @Delete
    suspend fun deleteDrink(drink: DrinkEntity)

    @Query("UPDATE drinks SET is_active = 0 WHERE id = :drinkId")
    suspend fun deactivateDrink(drinkId: Long)

    @Query("SELECT * FROM drinks WHERE is_custom = 1 AND is_active = 1 ORDER BY created_at DESC")
    fun getCustomDrinks(): Flow<List<DrinkEntity>>

    @Query("SELECT COUNT(*) FROM drinks WHERE is_custom = 0")
    suspend fun getDefaultDrinksCount(): Int

    @Query("DELETE FROM drinks WHERE is_custom = 1")
    suspend fun deleteAllCustomDrinks()
}