package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppThemeMode
import com.example.data.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "medikiosk_settings")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val KEY_EASY_MODE = booleanPreferencesKey("app_easy_mode")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_VOICE_ASSISTANT = booleanPreferencesKey("app_voice_assistant")
        val KEY_TTS_ENABLED = booleanPreferencesKey("app_tts_enabled")
        val KEY_VOICE_INTAKE = booleanPreferencesKey("app_voice_intake")
    }

    val voiceAssistantFlow: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_VOICE_ASSISTANT] ?: true }

    val ttsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_TTS_ENABLED] ?: true }

    val voiceIntakeFlow: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_VOICE_INTAKE] ?: true }

    val themeModeFlow: Flow<AppThemeMode> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            when (prefs[KEY_THEME_MODE]) {
                "LIGHT" -> AppThemeMode.LIGHT
                "DARK" -> AppThemeMode.DARK
                else -> AppThemeMode.SYSTEM
            }
        }

    val easyModeFlow: Flow<Boolean> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            prefs[KEY_EASY_MODE] ?: false
        }

    val languageFlow: Flow<Language> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val code = prefs[KEY_LANGUAGE] ?: "en"
            Language.entries.find { it.code == code } ?: Language.ENGLISH
        }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setEasyMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EASY_MODE] = enabled
        }
    }

    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.code
        }
    }

    suspend fun setVoiceAssistantEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VOICE_ASSISTANT] = enabled
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TTS_ENABLED] = enabled
        }
    }

    suspend fun setVoiceIntakeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VOICE_INTAKE] = enabled
        }
    }
}
