package com.example.terminal_test_app.data.settings


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("terminal_settings")

class SettingsDataSource(
    private val context: Context
) {
    private val ENV = stringPreferencesKey("env")
    private val KEY_ID = stringPreferencesKey("nexo_key_id")
    private val PASSPHRASE = stringPreferencesKey("nexo_passphrase")

    val settings: Flow<TerminalSettings> =
        context.dataStore.data.map { prefs ->
            TerminalSettings(
                environment = when (prefs[ENV]) {
                    "LIVE" -> TerminalEnvironment.LIVE
                    else -> TerminalEnvironment.TEST
                },
                nexoKeyIdentifier = prefs[KEY_ID],
                nexoPassphrase = prefs[PASSPHRASE]
            )
        }

    suspend fun update(settings: TerminalSettings) {
        context.dataStore.edit { prefs ->
            prefs[ENV] = settings.environment.name
            settings.nexoKeyIdentifier?.let { prefs[KEY_ID] = it }
            settings.nexoPassphrase?.let { prefs[PASSPHRASE] = it }
        }
    }
}
