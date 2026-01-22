package com.example.terminal_test_app.presentation.checkout

import android.hardware.camera2.CameraManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

@Composable
fun CheckoutScreen() {
    val context = LocalContext.current
    val viewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show status message if a terminal payment is in progress or finished
        when (val state = uiState) {
            is PaymentUiState.Loading -> {
                Text("Processing Terminal Payment...")
            }
            is PaymentUiState.Success -> {
                Text("Payment Successful! Auth: ${state.authCode}", color = Color.Green)
                Button(onClick = { viewModel.resetStatus() }) { Text("Back to Start") }
            }
            is PaymentUiState.Error -> {
                Text("Payment Failed: ${state.message}", color = Color.Red)
                Button(onClick = { viewModel.resetStatus() }) { Text("Try Again") }
            }
            is PaymentUiState.Idle -> {
                // Show your two main buttons here
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Existing Button: Opens WebView
        Button(
            modifier = Modifier.fillMaxWidth(0.8f),
            onClick = { viewModel.onCheckoutClicked() }
        ) {
            Text("Open Web Checkout")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // New Button: Calls Terminal API
        Button(
            modifier = Modifier.fillMaxWidth(0.8f),
            onClick = { viewModel.onTerminalPaymentClicked() },
            enabled = uiState !is PaymentUiState.Loading // Disable while processing
        ) {
            Text("Terminal Payment (Local)")
        }
    }
}