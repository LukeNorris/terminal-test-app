package com.example.terminal_test_app.domain.usecase

import com.example.terminal_test_app.domain.model.CheckoutPage
import com.example.terminal_test_app.domain.repository.CheckoutLauncher


class OpenCheckoutUseCase(
    private val checkoutLauncher: CheckoutLauncher
) {
    operator fun invoke() {
        checkoutLauncher.openCheckout(
            CheckoutPage(
                url = "https://www.mystoredemo.io/#/checkout"
            )
        )
    }
}
