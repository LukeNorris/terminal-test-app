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
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .callTimeout(65, java.util.concurrent.TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://127.0.0.1:8443/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTerminalApi(
        retrofit: Retrofit
    ): TerminalApi =
        retrofit.create(TerminalApi::class.java)

    // ✅ provideSettingsDataSource removed — Hilt uses the one from AppModule

    @Provides
    @Singleton
    fun provideTerminalRepository(
        api: TerminalApi,
        settingsDataSource: SettingsDataSource, // ✅ injected from AppModule
        @Named("saleId") saleId: String
    ): TerminalRepositoryImpl =
        TerminalRepositoryImpl(
            api = api,
            settingsDataSource = settingsDataSource,
            saleId = saleId
        )

    @Provides
    @Singleton
    fun provideBarcodeScanner(repository: TerminalRepositoryImpl): BarcodeScanner = repository

    @Provides
    @Singleton
    fun providePaymentRepository(repository: TerminalRepositoryImpl): PaymentRepository = repository
}