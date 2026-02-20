package com.example.terminal_test_app.domain.repository


import com.example.terminal_test_app.data.settings.TerminalSettings

interface SettingsRepository {
    suspend fun getSettings(): TerminalSettings
}