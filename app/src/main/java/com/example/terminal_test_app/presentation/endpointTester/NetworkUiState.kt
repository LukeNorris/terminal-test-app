package com.example.terminal_test_app.presentation.endpointTester

import com.example.terminal_test_app.domain.model.EndpointTestResult

data class NetworkUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val result: EndpointTestResult? = null
)
