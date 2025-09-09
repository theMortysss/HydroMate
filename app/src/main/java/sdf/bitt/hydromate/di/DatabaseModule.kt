package sdf.bitt.hydromate.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sdf.bitt.hydromate.data.local.HydroMateDatabase
import sdf.bitt.hydromate.data.local.dao.UserSettingsDao
import sdf.bitt.hydromate.data.local.dao.WaterEntryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHydroMateDatabase(
        @ApplicationContext context: Context
    ): HydroMateDatabase {
        return Room.databaseBuilder(
            context,
            HydroMateDatabase::class.java,
            HydroMateDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideWaterEntryDao(database: HydroMateDatabase): WaterEntryDao {
        return database.waterEntryDao()
    }

    @Provides
    fun provideUserSettingsDao(database: HydroMateDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }
}