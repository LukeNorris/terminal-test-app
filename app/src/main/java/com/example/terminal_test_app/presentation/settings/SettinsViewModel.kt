package com.example.terminal_test_app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.terminal_test_app.data.settings.SettingsDataSource
import com.example.terminal_test_app.data.settings.TerminalSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataSource: SettingsDataSource
) : ViewModel() {

    // Expose settings as a StateFlow for the UI
    val settings: StateFlow<TerminalSettings> = dataSource.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TerminalSettings()
    )

    fun updateSettings(newSettings: TerminalSettings) {
        viewModelScope.launch {
            dataSource.update(newSettings)
        }
    }
}