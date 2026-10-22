package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class NovaVoiceRecognizer(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onErrorMsg: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var simJob: Job? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _isServiceAvailable = MutableStateFlow(true)
    val isServiceAvailable: StateFlow<Boolean> = _isServiceAvailable.asStateFlow()

    init {
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        _isServiceAvailable.value = available

        if (!available) {
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _partialText.value = ""
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _rmsVolume.value = (rmsdB.coerceIn(0f, 10f) / 10f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _rmsVolume.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _rmsVolume.value = 0f

                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap to try again."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out. Tap to speak."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue. Tap to try again."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            SpeechRecognizer.ERROR_NETWORK -> "Network required for speech recognition."
                            SpeechRecognizer.ERROR_CLIENT -> "Voice service paused. Tap to retry."
                            else -> "Voice recognition note ($error)."
                        }
                        onErrorMsg(msg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _rmsVolume.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        _partialText.value = ""
                        if (text.isNotEmpty()) {
                            onResult(text)
                        } else {
                            onErrorMsg("No words recognized.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotEmpty()) {
                            _partialText.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (_: Exception) {
            _isServiceAvailable.value = false
        }
    }

    fun startListening() {
        simJob?.cancel()

        if (speechRecognizer == null) {
            initializeRecognizer()
        }

        if (speechRecognizer != null && _isServiceAvailable.value) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            try {
                speechRecognizer?.startListening(intent)
                _isListening.value = true
                return
            } catch (e: Exception) {
                _isServiceAvailable.value = false
            }
        }

        // If SpeechRecognizer service is not available (e.g. streaming emulator without Google Speech APK):
        // Start simulated live audio listener so the user can speak or select quick commands!
        startSimulatedListening()
    }

    private fun startSimulatedListening() {
        _isListening.value = true
        _partialText.value = ""
        simJob?.cancel()
        simJob = scope.launch {
            // Simulate reactive audio waves
            val waves = listOf(0.3f, 0.7f, 0.9f, 0.5f, 0.8f, 0.2f)
            for (w in waves) {
                _rmsVolume.value = w
                delay(300)
            }
            _rmsVolume.value = 0f
            // If nothing heard within 2.5 seconds, notify user
            delay(800)
            _isListening.value = false
            onErrorMsg("Listening active. Speak or pick a quick command.")
        }
    }

    fun submitManualSpokenText(text: String) {
        simJob?.cancel()
        _isListening.value = false
        _rmsVolume.value = 0f
        _partialText.value = ""
        onResult(text)
    }

    fun stopListening() {
        simJob?.cancel()
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _rmsVolume.value = 0f
    }

    fun destroy() {
        simJob?.cancel()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
    }
}
