package com.example.terminal_test_app.di

import android.content.Context
import com.example.terminal_test_app.domain.repository.CheckoutLauncher
import com.example.terminal_test_app.platform.checkout.AndroidCheckoutLauncher


object AppContainer {

    lateinit var checkoutLauncher: CheckoutLauncher
        private set

    fun init(appContext: Context){
        checkoutLauncher = AndroidCheckoutLauncher(appContext)
    }
}