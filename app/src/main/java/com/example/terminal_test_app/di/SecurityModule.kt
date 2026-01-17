package com.example.terminal_test_app.di

import android.content.Context
import com.example.terminal_test_app.data.remote.tls.AdyenTls
import com.example.terminal_test_app.data.settings.SettingsDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideTrustManager(
        @ApplicationContext context: Context,
        settingsDataSource: SettingsDataSource
    ): X509TrustManager =
        runBlocking {
            val settings = settingsDataSource.settings.first()
            AdyenTls.createTrustManager(context, settings.environment)
        }

    @Provides
    @Singleton
    fun provideSslContext(
        trustManager: X509TrustManager
    ): SSLContext =
        AdyenTls.createSslContext(trustManager)
}
