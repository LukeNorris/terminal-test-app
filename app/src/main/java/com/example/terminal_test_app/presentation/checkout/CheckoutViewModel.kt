package com.example.terminal_test_app.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.terminal_test_app.domain.usecase.MakePaymentUseCase
import com.example.terminal_test_app.domain.usecase.OpenCheckoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Success(val authCode: String) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val openCheckoutUseCase: OpenCheckoutUseCase,
    private val makePaymentUseCase: MakePaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState

    fun resetStatus() {
        _uiState.value = PaymentUiState.Idle
    }

    fun onCheckoutClicked() {
        openCheckoutUseCase()
    }

    fun onTerminalPaymentClicked() {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading

            val result = makePaymentUseCase(10.00)

            result.onSuccess {
                _uiState.value = PaymentUiState.Success(it.authCode ?: "Approved")
            }.onFailure {
                _uiState.value = PaymentUiState.Error(it.message ?: "Payment Failed")
            }
        }
    }
}
