package com.example.terminal_test_app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import java.util.UUID

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("saleId")
    fun provideSaleId(): String {
        return "Terminal1" // or UUID.randomUUID().toString()
    }
}