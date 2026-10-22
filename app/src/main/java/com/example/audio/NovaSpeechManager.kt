package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class NovaSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private var fallbackJob: Job? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var speechRate = 1.0f
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
        } catch (_: Exception) {}

        try {
            tts = TextToSpeech(appContext, this)
        } catch (_: Exception) {
            isInitialized = false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            try {
                val langResult = tts?.setLanguage(Locale.US)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
                isInitialized = true
                tts?.setSpeechRate(speechRate)
                setupProgressListener()
            } catch (_: Exception) {
                isInitialized = true
            }
        } else {
            isInitialized = false
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                scope.launch { _isSpeaking.value = true }
            }

            override fun onDone(utteranceId: String?) {
                scope.launch {
                    fallbackJob?.cancel()
                    _isSpeaking.value = false
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                scope.launch {
                    fallbackJob?.cancel()
                    _isSpeaking.value = false
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                scope.launch {
                    fallbackJob?.cancel()
                    _isSpeaking.value = false
                }
            }
        })
    }

    fun setSpeed(rate: Float) {
        speechRate = rate
        if (isInitialized) {
            try {
                tts?.setSpeechRate(rate)
            } catch (_: Exception) {}
        }
    }

    fun speak(
        text: String,
        utteranceId: String = "nova_${System.currentTimeMillis()}",
        onSpeechDone: (() -> Unit)? = null
    ) {
        stop()
        if (text.isBlank()) return

        _isSpeaking.value = true

        // Beep confirmation tone for auditory cue
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (_: Exception) {}

        var spokeViaTts = false
        if (isInitialized && tts != null) {
            try {
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                }
                val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                if (result == TextToSpeech.SUCCESS) {
                    spokeViaTts = true
                }
            } catch (_: Exception) {
                spokeViaTts = false
            }
        }

        // Realistic speaking duration timer (avg 60ms per character / rate)
        // Ensures waveform animation finishes smoothly even if emulator TTS has no sound driver
        val estimatedMs = ((text.length * 60L) / speechRate.coerceAtLeast(0.5f)).toLong().coerceIn(1200L, 12000L)
        fallbackJob?.cancel()
        fallbackJob = scope.launch {
            delay(estimatedMs)
            _isSpeaking.value = false
            onSpeechDone?.invoke()
        }
    }

    fun stop() {
        fallbackJob?.cancel()
        if (isInitialized) {
            try {
                tts?.stop()
            } catch (_: Exception) {}
        }
        _isSpeaking.value = false
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
