package com.example.terminal_test_app.presentation.checkout

import androidx.lifecycle.ViewModel
import com.example.terminal_test_app.domain.usecase.OpenCheckoutUseCase

class CheckoutViewModel(
    private val openCheckoutUseCase: OpenCheckoutUseCase
) : ViewModel() {

    fun onCheckoutClicked() {
        openCheckoutUseCase()
    }
}
