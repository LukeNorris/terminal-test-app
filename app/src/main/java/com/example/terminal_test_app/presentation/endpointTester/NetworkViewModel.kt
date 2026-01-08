package com.example.terminal_test_app.presentation.endpointTester

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.terminal_test_app.domain.usecase.TestEndpointUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NetworkViewModel(
    private val testEndpointUseCase: TestEndpointUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState

    fun onUrlChanged(value: String) {
        _uiState.update { it.copy(url = value) }
    }

    fun onTestClicked() {
        val url = _uiState.value.url
        if (url.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, result = null) }

            val result = testEndpointUseCase(url)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    result = result
                )
            }
        }
    }
}
