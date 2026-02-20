package com.example.terminal_test_app.domain.usecase


import com.example.terminal_test_app.domain.repository.SettingsRepository
import javax.inject.Inject

class ValidateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Boolean {
        val settings = settingsRepository.getSettings()
        return settings.poiId.isNotBlank()
                && settings.nexoKeyIdentifier.isNotBlank()
                && settings.nexoPassphrase.isNotBlank()
    }
}