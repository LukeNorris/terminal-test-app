package com.example.terminal_test_app.presentation.checkout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.terminal_test_app.domain.usecase.OpenCheckoutUseCase
import com.example.terminal_test_app.platform.checkout.AndroidCheckoutLauncher

class CheckoutViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val launcher = AndroidCheckoutLauncher(context)
        val useCase = OpenCheckoutUseCase(launcher)

        return CheckoutViewModel(useCase) as T
    }
}
