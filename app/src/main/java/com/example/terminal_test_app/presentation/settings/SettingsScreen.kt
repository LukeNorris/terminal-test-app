package com.example.terminal_test_app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.terminal_test_app.data.settings.TerminalEnvironment

@Composable
fun SettingsRoute() {
    val viewModel: SettingsViewModel = hiltViewModel()
    SettingsScreen(viewModel = viewModel)
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    var passwordVisible by remember { mutableStateOf(false) }

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
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Terminal Configuration",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = uiState.poiId,
                    onValueChange = {
                        viewModel.onSettingsChanged(uiState.copy(poiId = it))
                    },
                    label = { Text("POI ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.nexoKeyIdentifier,
                    onValueChange = {
                        viewModel.onSettingsChanged(uiState.copy(nexoKeyIdentifier = it))
                    },
                    label = { Text("Nexo Key Identifier") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.nexoPassphrase,
                    onValueChange = {
                        viewModel.onSettingsChanged(uiState.copy(nexoPassphrase = it))
                    },
                    label = { Text("Nexo Passphrase") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else
                                    Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible)
                                    "Hide password"
                                else
                                    "Show password"
                            )
                        }
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Environment")
                    FilterChip(
                        selected = uiState.environment == TerminalEnvironment.TEST,
                        onClick = {
                            viewModel.onSettingsChanged(
                                uiState.copy(environment = TerminalEnvironment.TEST)
                            )
                        },
                        label = { Text("TEST") }
                    )
                    FilterChip(
                        selected = uiState.environment == TerminalEnvironment.LIVE,
                        onClick = {
                            viewModel.onSettingsChanged(
                                uiState.copy(environment = TerminalEnvironment.LIVE)
                            )
                        },
                        label = { Text("LIVE") }
                    )
                }

                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "Save Settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}