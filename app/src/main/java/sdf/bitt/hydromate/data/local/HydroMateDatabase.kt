package sdf.bitt.hydromate.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import sdf.bitt.hydromate.data.local.dao.DrinkDao
import sdf.bitt.hydromate.data.local.dao.UserSettingsDao
import sdf.bitt.hydromate.data.local.dao.WaterEntryDao
import sdf.bitt.hydromate.data.local.entities.DrinkEntity
import sdf.bitt.hydromate.data.local.entities.UserSettingsEntity
import sdf.bitt.hydromate.data.local.entities.WaterEntryEntity

@Database(
    entities = [
        WaterEntryEntity::class,
        UserSettingsEntity::class,
        DrinkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HydroMateDatabase : RoomDatabase() {

    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun drinkDao(): DrinkDao

    companion object {
        const val DATABASE_NAME = "hydromate_database"
    }
}
