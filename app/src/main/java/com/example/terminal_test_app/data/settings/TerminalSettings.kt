package com.example.terminal_test_app.data.settings

enum class TerminalEnvironment {
    TEST,
    LIVE
}

data class TerminalSettings(
    val environment: TerminalEnvironment = TerminalEnvironment.TEST,
    val nexoKeyIdentifier: String? = null,
    val nexoPassphrase: String? = null
)
