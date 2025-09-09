package sdf.bitt.hydromate.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sdf.bitt.hydromate.data.repositories.UserSettingsRepositoryImpl
import sdf.bitt.hydromate.data.repositories.WaterRepositoryImpl
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
}