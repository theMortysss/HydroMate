package sdf.bitt.hydromate.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sdf.bitt.hydromate.data.repositories.ProfileRepositoryImpl
import sdf.bitt.hydromate.data.repository.AchievementRepositoryImpl
import sdf.bitt.hydromate.data.repository.ChallengeRepositoryImpl
import sdf.bitt.hydromate.data.repository.DrinkRepositoryImpl
import sdf.bitt.hydromate.data.repository.UserSettingsRepositoryImpl
import sdf.bitt.hydromate.data.repository.WaterRepositoryImpl
import sdf.bitt.hydromate.domain.repositories.AchievementRepository
import sdf.bitt.hydromate.domain.repositories.ChallengeRepository
import sdf.bitt.hydromate.domain.repositories.DrinkRepository
import sdf.bitt.hydromate.domain.repositories.ProfileRepository
import sdf.bitt.hydromate.domain.repositories.UserSettingsRepository
import sdf.bitt.hydromate.domain.repositories.WaterRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWaterRepository(
        waterRepositoryImpl: WaterRepositoryImpl
    ): WaterRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(
        userSettingsRepositoryImpl: UserSettingsRepositoryImpl
    ): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindDrinkRepository(
        drinkRepositoryImpl: DrinkRepositoryImpl
    ): DrinkRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        challengeRepositoryImpl: ChallengeRepositoryImpl
    ): ChallengeRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(
        achievementRepositoryImpl: AchievementRepositoryImpl
    ): AchievementRepository
}
