package com.example.terminal_test_app.di

import android.content.Context
import com.example.terminal_test_app.data.settings.SettingsDataSource
import com.example.terminal_test_app.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("saleId")
    fun provideSaleId(): String = "Terminal1"

    @Provides
    @Singleton
    fun provideSettingsDataSource(
        @ApplicationContext context: Context
    ): SettingsDataSource = SettingsDataSource(context)

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataSource: SettingsDataSource
    ): SettingsRepository = dataSource
}