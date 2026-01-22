package com.example.terminal_test_app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore // Required for delegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This property delegate must be at the top level of the file
// and only declared ONCE in the entire project.
private val Context.dataStore by preferencesDataStore("terminal_settings")

enum class TerminalEnvironment {
    TEST,
    LIVE
}

data class TerminalSettings(
    val environment: TerminalEnvironment = TerminalEnvironment.TEST,
    val poiId: String = "",
    val nexoKeyIdentifier: String = "",
    val nexoPassphrase: String = ""
)

class SettingsDataSource(private val context: Context) {
    private val ENV = stringPreferencesKey("env")
    private val POI_ID = stringPreferencesKey("poi_id")
    private val KEY_ID = stringPreferencesKey("nexo_key_id")
    private val PASSPHRASE = stringPreferencesKey("nexo_passphrase")

    val settings: Flow<TerminalSettings> = context.dataStore.data.map { prefs ->
        TerminalSettings(
            // Use valueOf with a fallback to handle enum parsing safely
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

    suspend fun update(settings: TerminalSettings) {
        context.dataStore.edit { prefs ->
            prefs[ENV] = settings.environment.name
            prefs[POI_ID] = settings.poiId
            prefs[KEY_ID] = settings.nexoKeyIdentifier
            prefs[PASSPHRASE] = settings.nexoPassphrase
        }
    }
}