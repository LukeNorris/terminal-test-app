package com.example.terminal_test_app.domain.repository

import com.example.terminal_test_app.domain.model.EndpointTestResult

interface EndpointTester {
    suspend fun test(url: String): EndpointTestResult
}