package com.example.terminal_test_app.presentation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.terminal_test_app.data.settings.SettingsDataSource
import com.example.terminal_test_app.data.settings.TerminalSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataSource: SettingsDataSource
) : ViewModel() {

    private val _saveEvents = MutableSharedFlow<Unit>()
    val saveEvents = _saveEvents.asSharedFlow()

    // Source of truth from disk
    val persistedSettings: StateFlow<TerminalSettings> = dataSource.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TerminalSettings()
    )

    // Local draft state for smooth typing
    var uiState by mutableStateOf(TerminalSettings())
        private set

    init {
        // Load the saved values into the UI draft when the screen opens
        viewModelScope.launch {
            uiState = persistedSettings.first()
        }
    }

    // This updates the local draft in memory
    fun onSettingsChanged(newSettings: TerminalSettings) {
        uiState = newSettings
    }


    fun saveSettings() {
        viewModelScope.launch {
            dataSource.update(uiState)
            _saveEvents.emit(Unit) // Signal that saving is complete
        }
    }
}