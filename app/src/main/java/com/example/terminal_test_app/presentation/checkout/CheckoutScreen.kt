package com.example.terminal_test_app.presentation.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CheckoutScreen() {

    val viewModel: CheckoutViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                // idle state
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(0.8f),
            onClick = { viewModel.onCheckoutClicked() }
        ) {
            Text("Open Web Checkout")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier.fillMaxWidth(0.8f),
            onClick = { viewModel.onTerminalPaymentClicked() },
            enabled = uiState !is PaymentUiState.Loading
        ) {
            Text("Terminal Payment (Local)")
        }
    }
}
