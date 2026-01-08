package com.example.terminal_test_app.presentation.endpointTester


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.terminal_test_app.domain.usecase.TestEndpointUseCase
import com.example.terminal_test_app.platform.endPointTest.AndroidEndpointTester

class NetworkViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NetworkViewModel::class.java)) {

            val tester = AndroidEndpointTester()
            val useCase = TestEndpointUseCase(tester)

            @Suppress("UNCHECKED_CAST")
            return NetworkViewModel(useCase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
