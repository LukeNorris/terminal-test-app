package com.example.terminal_test_app.presentation.endpointTester

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.terminal_test_app.domain.model.EndpointTestResult
import androidx.compose.runtime.getValue


@Composable
fun NetworkTestScreen(
    viewModel: NetworkViewModel = viewModel(factory = NetworkViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.padding(16.dp)) {

        TextField(
            value = uiState.url,
            onValueChange = viewModel::onUrlChanged,
            label = { Text("Endpoint URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = viewModel::onTestClicked,
            enabled = !uiState.isLoading
        ) {
            Text("Test Connectivity")
        }

        Spacer(Modifier.height(16.dp))

        uiState.result?.let { result ->
            when (result) {
                is EndpointTestResult.Success -> {
                    Text("✅ Success")
                    Text("Status: ${result.statusCode}")
                    Text("Latency: ${result.latencyMs} ms")
                }

                is EndpointTestResult.Failure -> {
                    Text("❌ Failed")
                    Text(result.reason)
                }
            }
        }
    }
}
