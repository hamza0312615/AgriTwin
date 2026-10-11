package com.example.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Global reactive Text-to-Speech manager for AgriTwin.
 * Supports spoken audio readout of Daily Agronomic Briefings and Weekly Reports
 * in both Urdu and English depending on the selected language setting.
 */
object AppTtsManager {
    private const val TAG = "AgriTwinTTS"
    private var ttsEngine: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: Triple<String, String, Boolean>? = null

    var isSpeaking by mutableStateOf(false)
        private set

    var currentUtteranceId by mutableStateOf<String?>(null)
        private set

    fun initialize(context: Context) {
        if (ttsEngine != null) return

        ttsEngine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                val tts = ttsEngine
                if (tts != null) {
                    tts.setSpeechRate(0.95f)

                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            isSpeaking = true
                            currentUtteranceId = utteranceId
                        }

                        override fun onDone(utteranceId: String?) {
                            isSpeaking = false
                            currentUtteranceId = null
                        }

                        override fun onError(utteranceId: String?) {
                            isSpeaking = false
                            currentUtteranceId = null
                            Log.e(TAG, "TTS utterance error: $utteranceId")
                        }
                    })

                    // Handle speech request queued prior to engine readiness
                    pendingSpeech?.let { (en, ur, urdu) ->
                        pendingSpeech = null
                        speakInternal(en, ur, urdu, "pending_tts")
                    }
                }
            } else {
                Log.e(TAG, "Failed to initialize TextToSpeech: status $status")
            }
        }
    }

    /**
     * Speak the provided text based on the active language setting.
     * Tapping while speaking stops audio playback immediately.
     */
    fun speak(
        context: Context,
        textEn: String,
        textUr: String,
        isUrdu: Boolean = AppLanguageState.isUrdu,
        utteranceId: String = "agritwin_tts"
    ) {
        if (ttsEngine == null) {
            initialize(context)
        }

        if (isSpeaking) {
            stop()
            return
        }

        if (!isInitialized) {
            pendingSpeech = Triple(textEn, textUr, isUrdu)
            return
        }

        speakInternal(textEn, textUr, isUrdu, utteranceId)
    }

    private fun speakInternal(
        textEn: String,
        textUr: String,
        isUrdu: Boolean,
        utteranceId: String
    ) {
        val tts = ttsEngine ?: return

        val targetText = if (isUrdu) textUr else textEn

        if (isUrdu) {
            val urPk = Locale("ur", "PK")
            val urGeneric = Locale("ur")
            val availablePk = tts.isLanguageAvailable(urPk)
            val availableGeneric = tts.isLanguageAvailable(urGeneric)
            when {
                availablePk >= TextToSpeech.LANG_AVAILABLE -> tts.language = urPk
                availableGeneric >= TextToSpeech.LANG_AVAILABLE -> tts.language = urGeneric
                else -> {
                    val result = tts.setLanguage(urPk)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts.language = Locale.UK
                    }
                }
            }
        } else {
            tts.language = Locale.UK
        }

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        tts.speak(targetText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        ttsEngine?.stop()
        isSpeaking = false
        currentUtteranceId = null
        pendingSpeech = null
    }

    fun shutdown() {
        stop()
        ttsEngine?.shutdown()
        ttsEngine = null
        isInitialized = false
    }
}
