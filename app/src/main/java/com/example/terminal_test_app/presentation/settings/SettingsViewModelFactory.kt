package com.example.terminal_test_app.presentation.settings

import com.example.terminal_test_app.data.settings.SettingsDataSource

class SettingsViewModelFactory(private val dataSource: SettingsDataSource) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(dataSource) as T
    }
}