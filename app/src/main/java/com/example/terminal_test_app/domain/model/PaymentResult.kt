package com.example.terminal_test_app.domain.model

// domain/model/PaymentResult.kt
data class PaymentResult(
    val success: Boolean,
    val authCode: String? = null,
    val message: String? = null
)

// domain/repository/PaymentRepository.kt
interface PaymentRepository {
    suspend fun makePayment(amount: Double, currency: String): Result<PaymentResult>
}