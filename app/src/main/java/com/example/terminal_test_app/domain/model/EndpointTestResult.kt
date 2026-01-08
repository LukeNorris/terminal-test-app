package com.example.terminal_test_app.domain.model

sealed class EndpointTestResult {
    data class Success(
        val statusCode: Int,
        val latencyMs: Long
    ) : EndpointTestResult()

    data class Failure(
        val reason: String
    ) : EndpointTestResult()
}