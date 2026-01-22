package com.example.terminal_test_app.presentation.settings

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import com.example.terminal_test_app.data.settings.SettingsDataSource

@Composable
fun SettingsRoute() {
    // Because we aren't using Hilt for this specific ViewModel yet,
    // we create the factory manually here
    val context = LocalContext.current
    val dataSource = SettingsDataSource(context)
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(dataSource)
    )

    SettingsScreen(viewModel = viewModel)
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Terminal Configuration", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = settings.poiId,
            onValueChange = { viewModel.updateSettings(settings.copy(poiId = it)) },
            label = { Text("POI ID (Terminal ID)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = settings.nexoKeyIdentifier,
            onValueChange = { viewModel.updateSettings(settings.copy(nexoKeyIdentifier = it)) },
            label = { Text("Nexo Key Identifier") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = settings.nexoPassphrase,
            onValueChange = { viewModel.updateSettings(settings.copy(nexoPassphrase = it)) },
            label = { Text("Nexo Passphrase") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Text(
            text = "These values are used to encrypt Nexo requests sent to the terminal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}