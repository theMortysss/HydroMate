package dev.techm1nd.hydromate.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.techm1nd.hydromate.data.local.HydroMateDatabase
import dev.techm1nd.hydromate.data.local.dao.AchievementDao
import dev.techm1nd.hydromate.data.local.dao.ChallengeDao
import dev.techm1nd.hydromate.data.local.dao.DrinkDao
import dev.techm1nd.hydromate.data.local.dao.UserProfileDao
import dev.techm1nd.hydromate.data.local.dao.UserSettingsDao
import dev.techm1nd.hydromate.data.local.dao.WaterEntryDao
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

    @Provides
    fun provideDrinkDao(database: HydroMateDatabase): DrinkDao {
        return database.drinkDao()
    }

    // NEW PROVIDES
    @Provides
    fun provideUserProfileDao(database: HydroMateDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideChallengeDao(database: HydroMateDatabase): ChallengeDao {
        return database.challengeDao()
    }

    @Provides
    fun provideAchievementDao(database: HydroMateDatabase): AchievementDao {
        return database.achievementDao()
    }
}