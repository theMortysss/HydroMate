package dev.techm1nd.hydromate.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.techm1nd.hydromate.data.repositories.ProfileRepositoryImpl
import dev.techm1nd.hydromate.data.repository.AchievementRepositoryImpl
import dev.techm1nd.hydromate.data.repository.AuthRepositoryImpl
import dev.techm1nd.hydromate.data.repository.ChallengeRepositoryImpl
import dev.techm1nd.hydromate.data.repository.DrinkRepositoryImpl
import dev.techm1nd.hydromate.data.repository.SyncRepositoryImpl
import dev.techm1nd.hydromate.data.repository.TipsRepositoryImpl
import dev.techm1nd.hydromate.data.repository.UserSettingsRepositoryImpl
import dev.techm1nd.hydromate.data.repository.WaterRepositoryImpl
import dev.techm1nd.hydromate.domain.repositories.AchievementRepository
import dev.techm1nd.hydromate.domain.repositories.AuthRepository
import dev.techm1nd.hydromate.domain.repositories.ChallengeRepository
import dev.techm1nd.hydromate.domain.repositories.DrinkRepository
import dev.techm1nd.hydromate.domain.repositories.ProfileRepository
import dev.techm1nd.hydromate.domain.repositories.SyncRepository
import dev.techm1nd.hydromate.domain.repositories.TipsRepository
import dev.techm1nd.hydromate.domain.repositories.UserSettingsRepository
import dev.techm1nd.hydromate.domain.repositories.WaterRepository
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

    @Binds
    @Singleton
    abstract fun bindTipsRepository(
        tipsRepositoryImpl: TipsRepositoryImpl
    ): TipsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepositoryImpl: SyncRepositoryImpl
    ): SyncRepository
}
