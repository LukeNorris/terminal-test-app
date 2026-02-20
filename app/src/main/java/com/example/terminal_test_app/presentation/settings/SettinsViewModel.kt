package com.example.terminal_test_app.presentation.settings


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.terminal_test_app.data.settings.SettingsDataSource
import com.example.terminal_test_app.data.settings.TerminalSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataSource: SettingsDataSource
) : ViewModel() {

    private val _saveEvents = MutableSharedFlow<Unit>()
    val saveEvents = _saveEvents.asSharedFlow()

    var uiState by mutableStateOf(TerminalSettings())
        private set

    init {
        viewModelScope.launch {
            uiState = dataSource.settings.first()
        }
    }

    fun onSettingsChanged(newSettings: TerminalSettings) {
        uiState = newSettings
    }

    fun saveSettings() {
        viewModelScope.launch {
            dataSource.update(uiState)
            _saveEvents.emit(Unit)
        }
    }
}