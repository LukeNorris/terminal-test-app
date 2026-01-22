package com.example.terminal_test_app.domain.usecase

import com.example.terminal_test_app.domain.model.PaymentResult
import com.example.terminal_test_app.domain.repository.PaymentRepository // Check this import
import javax.inject.Inject

class MakePaymentUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(amount: Double): Result<PaymentResult> {
        return repository.makePayment(amount, "EUR")
    }
}