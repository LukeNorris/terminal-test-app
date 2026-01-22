package com.example.terminal_test_app.domain.repository


import com.example.terminal_test_app.domain.model.PaymentResult

interface PaymentRepository {
    suspend fun makePayment(amount: Double, currency: String): Result<PaymentResult>
}