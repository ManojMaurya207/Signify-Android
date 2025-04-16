package com.example.signify.ui.utils


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.voiceDataStore by preferencesDataStore("voice_prefs")

object VoicePreference {
    private val SELECTED_VOICE = stringPreferencesKey("selected_voice")

    suspend fun saveVoiceName(context: Context, voiceName: String) {
        context.voiceDataStore.edit { prefs ->
            prefs[SELECTED_VOICE] = voiceName
        }
    }

    suspend fun getSavedVoiceName(context: Context): String? {
        return context.voiceDataStore.data
            .map { it[SELECTED_VOICE] }
            .first()
    }
}
