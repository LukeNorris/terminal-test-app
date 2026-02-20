package com.example.terminal_test_app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.terminal_test_app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("terminal_settings")


class SettingsDataSource(private val context: Context) : SettingsRepository {

    private val ENV = stringPreferencesKey("env")
    private val POI_ID = stringPreferencesKey("poi_id")
    private val KEY_ID = stringPreferencesKey("nexo_key_id")
    private val PASSPHRASE = stringPreferencesKey("nexo_passphrase")

    val settings: Flow<TerminalSettings> = context.dataStore.data.map { prefs ->
        TerminalSettings(
            environment = try {
                TerminalEnvironment.valueOf(prefs[ENV] ?: "TEST")
            } catch (e: Exception) {
                TerminalEnvironment.TEST
            },
            poiId = prefs[POI_ID] ?: "",
            nexoKeyIdentifier = prefs[KEY_ID] ?: "",
            nexoPassphrase = prefs[PASSPHRASE] ?: ""
        )
    }

    override suspend fun getSettings(): TerminalSettings = settings.first()

    suspend fun update(settings: TerminalSettings) {
        context.dataStore.edit { prefs ->
            prefs[ENV] = settings.environment.name
            prefs[POI_ID] = settings.poiId
            prefs[KEY_ID] = settings.nexoKeyIdentifier
            prefs[PASSPHRASE] = settings.nexoPassphrase
        }
    }
}