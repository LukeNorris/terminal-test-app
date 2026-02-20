package com.example.terminal_test_app.data.settings

data class TerminalSettings(
    val environment: TerminalEnvironment = TerminalEnvironment.TEST,
    val poiId: String = "",
    val nexoKeyIdentifier: String = "",
    val nexoPassphrase: String = ""
)