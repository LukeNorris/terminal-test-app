package com.example.terminal_test_app.domain.usecase

import com.example.terminal_test_app.domain.model.EndpointTestResult
import com.example.terminal_test_app.domain.repository.EndpointTester

class TestEndpointUseCase(
    private val tester: EndpointTester
) {
    suspend operator fun invoke(url:String): EndpointTestResult {
        return tester.test(url)
    }
}