package com.example.terminal_test_app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.terminal_test_app.data.settings.SettingsDataSource
import com.example.terminal_test_app.data.settings.TerminalEnvironment

@Composable
fun SettingsRoute() {
    val context = LocalContext.current
    val dataSource = remember { SettingsDataSource(context) }
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(dataSource)
    )

    SettingsScreen(viewModel = viewModel)
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for the save event to show the confirmation
    LaunchedEffect(viewModel.saveEvents) {
        viewModel.saveEvents.collect {
            snackbarHostState.showSnackbar(
                message = "Settings saved successfully",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Terminal Configuration",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Configure the connection to the Adyen Terminal. These values are required for Nexo encryption.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // POI ID Field
            OutlinedTextField(
                value = uiState.poiId,
                onValueChange = { viewModel.onSettingsChanged(uiState.copy(poiId = it)) },
                label = { Text("POI ID (Terminal ID)") },
                placeholder = { Text("e.g. V400m-324688179") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Key Identifier Field
            OutlinedTextField(
                value = uiState.nexoKeyIdentifier,
                onValueChange = { viewModel.onSettingsChanged(uiState.copy(nexoKeyIdentifier = it)) },
                label = { Text("Nexo Key Identifier") },
                placeholder = { Text("e.g. MyKeyInstance01") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Passphrase Field
            OutlinedTextField(
                value = uiState.nexoPassphrase,
                onValueChange = { viewModel.onSettingsChanged(uiState.copy(nexoPassphrase = it)) },
                label = { Text("Nexo Passphrase") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            // Environment Selector (Optional addition based on your enum)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Environment", modifier = Modifier.padding(top = 12.dp))
                FilterChip(
                    selected = uiState.environment == TerminalEnvironment.TEST,
                    onClick = { viewModel.onSettingsChanged(uiState.copy(environment = TerminalEnvironment.TEST)) },
                    label = { Text("TEST") }
                )
                FilterChip(
                    selected = uiState.environment == TerminalEnvironment.LIVE,
                    onClick = { viewModel.onSettingsChanged(uiState.copy(environment = TerminalEnvironment.LIVE)) },
                    label = { Text("LIVE") }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Settings", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}