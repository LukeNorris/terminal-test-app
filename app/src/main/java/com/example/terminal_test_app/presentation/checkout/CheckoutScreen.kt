package com.example.terminal_test_app.presentation.checkout

import android.hardware.camera2.CameraManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CheckoutScreen() {
    val context = LocalContext.current

    val viewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModelFactory(context)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { viewModel.onCheckoutClicked() }
        ) {
            Text("Open Checkout")
        }
    }
}
