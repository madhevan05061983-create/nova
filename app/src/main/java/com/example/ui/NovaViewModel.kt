package com.example.ui

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.NovaSpeechManager
import com.example.audio.NovaVoiceRecognizer
import com.example.auth.FirebaseAuthHelper
import com.example.data.NovaBrain
import com.example.data.NovaRepository
import com.example.model.MessageRole
import com.example.model.NovaMessage
import com.example.model.UserSession
import com.example.util.AppLauncher
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("nova_auth_prefs", Context.MODE_PRIVATE)
    private val speechManager = NovaSpeechManager(application)
    private val repository = NovaRepository(application)
    private var voiceRecognizer: NovaVoiceRecognizer? = null
    private var firestoreSyncJob: Job? = null

    val isSpeaking = speechManager.isSpeaking

    // User authentication session state (No public gmail exposed)
    private val _userSession = MutableStateFlow(
        UserSession(
            name = prefs.getString("user_name", "Madhevan") ?: "Madhevan",
            email = prefs.getString("user_email", "operator@nova.local") ?: "operator@nova.local",
            isSignedIn = prefs.getBoolean("is_signed_in", true)
        )
    )
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private val _messages = MutableStateFlow<List<NovaMessage>>(
        listOf(
            NovaMessage(
                role = MessageRole.ASSISTANT,
                content = "Hello, I am Nova. Tap the glowing orb, speak any app name to open it (e.g. 'Open Camera', 'Open Chrome', 'Open Settings'), or check your battery.",
                intent = "general"
            )
        )
    )
    val messages: StateFlow<List<NovaMessage>> = _messages.asStateFlow()

    private val _statusText = MutableStateFlow("Ready and waiting for your command.")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _suggestions = MutableStateFlow(
        listOf(
            "Open Camera",
            "Open Settings",
            "What's my battery level?",
            "Open YouTube",
            "Open Google",
            "Tell me a joke"
        )
    )
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _autoSpeak = MutableStateFlow(true)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _isVideoPlaying = MutableStateFlow(true)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    // Dedicated Live Conversation Mode State
    private val _isLiveMode = MutableStateFlow(false)
    val isLiveMode: StateFlow<Boolean> = _isLiveMode.asStateFlow()

    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _isSpeechServiceAvailable = MutableStateFlow(true)
    val isSpeechServiceAvailable: StateFlow<Boolean> = _isSpeechServiceAvailable.asStateFlow()

    init {
        initVoiceRecognizer()
        initFirestoreSync()
    }

    private fun initFirestoreSync() {
        val uid = repository.currentUserId() ?: "offline_operator"
        firestoreSyncJob?.cancel()
        firestoreSyncJob = viewModelScope.launch {
            try {
                repository.observeConversations(uid).collect { firestoreMessages ->
                    if (firestoreMessages.isNotEmpty()) {
                        _messages.value = firestoreMessages
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun initVoiceRecognizer() {
        voiceRecognizer = NovaVoiceRecognizer(
            context = getApplication(),
            onResult = { transcript ->
                _isListening.value = false
                submitQuery(transcript)
            },
            onErrorMsg = { err ->
                _isListening.value = false
                _statusText.value = err
            }
        )

        viewModelScope.launch {
            voiceRecognizer?.isListening?.collect { listening ->
                _isListening.value = listening
                if (listening) {
                    speechManager.stop()
                    _statusText.value = "Listening... Speak now"
                }
            }
        }

        viewModelScope.launch {
            voiceRecognizer?.rmsVolume?.collect { vol ->
                _rmsVolume.value = vol
            }
        }

        viewModelScope.launch {
            voiceRecognizer?.partialText?.collect { text ->
                _partialTranscript.value = text
                if (text.isNotBlank()) {
                    _statusText.value = "Heard: \"$text\""
                }
            }
        }

        viewModelScope.launch {
            voiceRecognizer?.isServiceAvailable?.collect { avail ->
                _isSpeechServiceAvailable.value = avail
            }
        }
    }

    fun signIn(name: String, email: String) {
        _userSession.value = UserSession(name = name, email = email, isSignedIn = true)
        prefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putBoolean("is_signed_in", true)
            .apply()
        _statusText.value = "Signed in as $name."
        initFirestoreSync()
        if (_autoSpeak.value) {
            speechManager.speak("Welcome back, $name. Nova is ready for your commands.")
        }
    }

    fun signOut() {
        val userName = _userSession.value.name
        _userSession.value = UserSession(name = userName, email = _userSession.value.email, isSignedIn = false)
        prefs.edit().putBoolean("is_signed_in", false).apply()
        firestoreSyncJob?.cancel()
        stopSpeaking()
        if (_isListening.value) {
            voiceRecognizer?.stopListening()
        }
        _isLiveMode.value = false
        _statusText.value = "Signed out securely."

        val credentialManager = CredentialManager.create(getApplication())
        FirebaseAuthHelper.signOut(credentialManager, onComplete = {}, scope = viewModelScope)
    }

    fun submitQuery(queryText: String) {
        val trimmed = queryText.trim()
        if (trimmed.isEmpty()) return

        val uid = repository.currentUserId() ?: "offline_operator"

        // 1. Add user message
        val userMsg = NovaMessage(
            role = MessageRole.USER,
            content = trimmed
        )
        _messages.value = _messages.value + userMsg
        _statusText.value = "Nova is processing..."

        viewModelScope.launch {
            repository.saveConversationMessage(uid, userMsg)
        }

        // 2. Process query
        viewModelScope.launch {
            val brainResult = NovaBrain.processQuery(trimmed, _userSession.value.name)

            var replyToUse = brainResult.reply

            // Handle battery check task dynamically
            if (brainResult.intent == "battery") {
                val level = AppLauncher.getBatteryLevel(getApplication())
                replyToUse = if (level >= 0) {
                    "Your device battery is currently at $level percent."
                } else {
                    "Battery is charged and operating normally."
                }
            }

            // Handle universal app launch task
            if (brainResult.intent == "open_app") {
                val appQuery = brainResult.appQuery ?: ""
                val launchResult = AppLauncher.launchAppByName(getApplication(), appQuery)
                if (launchResult.success) {
                    _statusText.value = launchResult.message
                    replyToUse = "Opening ${launchResult.appName} for you now."
                }
            }

            // Handle note persistence
            if (brainResult.intent == "note") {
                repository.saveNote(uid, brainResult.reply)
            }

            val assistantMsg = NovaMessage(
                role = MessageRole.ASSISTANT,
                content = replyToUse,
                actionUrl = brainResult.actionUrl,
                actionLabel = brainResult.actionLabel,
                intent = brainResult.intent
            )

            _messages.value = _messages.value + assistantMsg
            _suggestions.value = brainResult.suggestions
            _statusText.value = if (brainResult.actionLabel != null) {
                "Task: ${brainResult.actionLabel}"
            } else {
                "Nova answered"
            }

            repository.saveConversationMessage(uid, assistantMsg)

            // 3. Check for voice sign out intent
            if (brainResult.intent == "sign_out") {
                speechManager.speak(
                    text = replyToUse,
                    onSpeechDone = {
                        signOut()
                    }
                )
                return@launch
            }

            // 4. Auto-speak response
            if (_autoSpeak.value || _isLiveMode.value) {
                speechManager.speak(
                    text = replyToUse,
                    onSpeechDone = {
                        // In Live Conversation mode, loop back to listening continuously!
                        if (_isLiveMode.value && !_isMicMuted.value) {
                            viewModelScope.launch {
                                delay(600)
                                if (_isLiveMode.value && !isSpeaking.value) {
                                    toggleListening()
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    fun toggleListening() {
        if (_isListening.value) {
            voiceRecognizer?.stopListening()
            _isListening.value = false
            _statusText.value = "Listening paused."
        } else {
            speechManager.stop()
            _statusText.value = "Microphone active. Speak now..."
            voiceRecognizer?.startListening()
        }
    }

    fun speakText(text: String) {
        speechManager.speak(text)
    }

    fun stopSpeaking() {
        speechManager.stop()
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        speechManager.setSpeed(rate)
    }

    fun setAutoSpeak(enabled: Boolean) {
        _autoSpeak.value = enabled
    }

    fun toggleVideoPlayback() {
        _isVideoPlaying.value = !_isVideoPlaying.value
    }

    fun clearChatHistory() {
        val uid = repository.currentUserId() ?: "offline_operator"
        _messages.value = listOf(
            NovaMessage(
                role = MessageRole.ASSISTANT,
                content = "Conversation history cleared. Ready for your commands.",
                intent = "general"
            )
        )
        speechManager.stop()
        _statusText.value = "History cleared."
        viewModelScope.launch {
            repository.clearHistory(uid)
        }
    }

    // Live Conversation Mode controls
    fun enterLiveMode() {
        _isLiveMode.value = true
        _statusText.value = "Nova Live Mode active. Speak naturally."
        speechManager.speak("Nova Live conversation activated. I am listening.", onSpeechDone = {
            if (!_isMicMuted.value) {
                toggleListening()
            }
        })
    }

    fun exitLiveMode() {
        _isLiveMode.value = false
        stopSpeaking()
        if (_isListening.value) {
            voiceRecognizer?.stopListening()
        }
        _statusText.value = "Exited Live Mode."
    }

    fun toggleLiveMicMute() {
        _isMicMuted.value = !_isMicMuted.value
        if (_isMicMuted.value && _isListening.value) {
            voiceRecognizer?.stopListening()
        } else if (!_isMicMuted.value && !isSpeaking.value) {
            toggleListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
        voiceRecognizer?.destroy()
        firestoreSyncJob?.cancel()
    }
}
