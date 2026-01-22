package com.example.terminal_test_app.presentation.checkout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.terminal_test_app.data.remote.api.TerminalApi
import com.example.terminal_test_app.data.repository.TerminalRepositoryImpl
import com.example.terminal_test_app.data.settings.SettingsDataSource
import com.example.terminal_test_app.domain.repository.PaymentRepository
import com.example.terminal_test_app.domain.usecase.MakePaymentUseCase
import com.example.terminal_test_app.domain.usecase.OpenCheckoutUseCase
import com.example.terminal_test_app.platform.checkout.AndroidCheckoutLauncher
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class CheckoutViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 1. Setup Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://127.0.0.1:8443")
            // ScalarsConverter MUST be added before Gson for NexoCrypto strings
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TerminalApi::class.java)

        // 2. Instantiate SettingsDataSource
        val settingsDataSource = SettingsDataSource(context)

        // 3. Instantiate the Repository with SettingsDataSource
        val terminalRepo = TerminalRepositoryImpl(
            api = api,
            settingsDataSource = settingsDataSource,
            saleId = "Terminal1" // This could also be moved to settings later
        )

        val makePaymentUseCase = MakePaymentUseCase(terminalRepo)
        val openCheckoutUseCase = OpenCheckoutUseCase(AndroidCheckoutLauncher(context))

        return CheckoutViewModel(
            openCheckoutUseCase = openCheckoutUseCase,
            makePaymentUseCase = makePaymentUseCase
        ) as T
    }
}