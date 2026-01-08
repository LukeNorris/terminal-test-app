package com.example.terminal_test_app.domain.repository

import com.example.terminal_test_app.domain.model.CheckoutPage


interface CheckoutLauncher {
 fun openCheckout(page: CheckoutPage)
}