package com.example.terminal_test_app.presentation.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CheckoutScreen(
    onNavigateToSettings: () -> Unit
) {
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
                // No button — auto-dismisses after 5 seconds
                Text(
                    "Payment Successful! Auth: ${state.authCode}",
                    color = Color.Green,
                    textAlign = TextAlign.Center
                )
            }

            is PaymentUiState.Error -> {
                // No button — auto-dismisses after 5 seconds
                Text(
                    "Payment Failed: ${state.message}",
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }

            is PaymentUiState.MissingSettings -> {
                Text(
                    "Terminal not configured. Please enter your credentials in Settings.",
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    viewModel.resetStatus()
                    onNavigateToSettings()
                }) {
                    Text("Go to Settings")
                }
            }

            is PaymentUiState.Idle -> { /* nothing shown */ }
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