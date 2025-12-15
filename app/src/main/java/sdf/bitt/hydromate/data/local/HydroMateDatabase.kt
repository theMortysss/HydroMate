package sdf.bitt.hydromate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import sdf.bitt.hydromate.data.local.dao.*
import sdf.bitt.hydromate.data.local.entities.*

@Database(
    entities = [
        WaterEntryEntity::class,
        UserSettingsEntity::class,
        DrinkEntity::class,
        // NEW: Profile, Challenges, Achievements
        UserProfileEntity::class,
        ChallengeEntity::class,
        AchievementEntity::class
    ],
    version = 2, // UPDATED VERSION
    exportSchema = false
)
abstract class HydroMateDatabase : RoomDatabase() {

    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun drinkDao(): DrinkDao

    // NEW DAOS
    abstract fun userProfileDao(): UserProfileDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val DATABASE_NAME = "hydromate_database"
    }
}
