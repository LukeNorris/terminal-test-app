package com.example.terminal_test_app.di

import com.example.terminal_test_app.domain.repository.CheckoutLauncher
import com.example.terminal_test_app.platform.checkout.AndroidCheckoutLauncher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CheckoutModule {

    @Binds
    @Singleton
    abstract fun bindCheckoutLauncher(
        impl: AndroidCheckoutLauncher
    ): CheckoutLauncher
}
