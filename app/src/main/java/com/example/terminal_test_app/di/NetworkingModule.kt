package com.example.terminal_test_app.di

import com.example.terminal_test_app.data.remote.api.TerminalApi
import com.example.terminal_test_app.data.repository.TerminalRepositoryImpl
import com.example.terminal_test_app.domain.repository.BarcodeScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
            .hostnameVerifier { host, _ ->
                host == "127.0.0.1" || host == "localhost"
            }
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://127.0.0.1:8443/")
            .client(okHttpClient)
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
    fun provideTerminalRepository(
        api: TerminalApi,
        @Named("saleId") saleId: String,
        @Named("poiId") poiId: String
    ): BarcodeScanner =
        TerminalRepositoryImpl(
            api = api,
            saleId = saleId,
            poiId = poiId
        )
}
