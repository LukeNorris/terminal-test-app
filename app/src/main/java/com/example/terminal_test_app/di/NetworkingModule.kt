package com.example.terminal_test_app.di

import android.content.Context
import com.example.terminal_test_app.data.remote.api.TerminalApi
import com.example.terminal_test_app.data.repository.TerminalRepositoryImpl
import com.example.terminal_test_app.data.settings.SettingsDataSource
import com.example.terminal_test_app.domain.repository.BarcodeScanner
import com.example.terminal_test_app.domain.repository.PaymentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        sslContext: SSLContext,
        trustManager: X509TrustManager
    ): OkHttpClient =
        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager) // CRITICAL
            .hostnameVerifier { _, _ -> true } // CRITICAL
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://127.0.0.1:8443/")
            .client(okHttpClient)
            // ScalarsConverterFactory MUST come before Gson to handle raw encrypted strings
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTerminalApi(
        retrofit: Retrofit
    ): TerminalApi =
        retrofit.create(TerminalApi::class.java)

    @Provides
    @Singleton
    fun provideSettingsDataSource(
        @ApplicationContext context: Context
    ): SettingsDataSource = SettingsDataSource(context)

    @Provides
    @Singleton
    fun provideTerminalRepository(
        api: TerminalApi,
        settingsDataSource: SettingsDataSource,
        @Named("saleId") saleId: String
    ): TerminalRepositoryImpl =
        TerminalRepositoryImpl(
            api = api,
            settingsDataSource = settingsDataSource,
            saleId = saleId
        )

    // Binds the single repository instance to the BarcodeScanner interface
    @Provides
    @Singleton
    fun provideBarcodeScanner(repository: TerminalRepositoryImpl): BarcodeScanner = repository

    // Binds the same repository instance to the PaymentRepository interface
    @Provides
    @Singleton
    fun providePaymentRepository(repository: TerminalRepositoryImpl): PaymentRepository = repository
}