package dev.techm1nd.hydromate.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SnackbarModule {

    @Provides
    @Singleton
    fun provideGlobalSnackbarController(): GlobalSnackbarController {
        return GlobalSnackbarController()
    }
}