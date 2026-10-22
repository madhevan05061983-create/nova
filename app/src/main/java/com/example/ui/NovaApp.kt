package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.NovaAuthScreen
import com.example.ui.components.NovaChatTimeline
import com.example.ui.components.NovaControlsPanel
import com.example.ui.components.NovaLiveConversation
import com.example.ui.components.NovaVoiceVisualizer
import com.example.ui.components.QuickSuggestionRow
import com.example.ui.components.VideoBackground
import com.example.ui.components.WebEditionView
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaDeepSpace
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaSlateText
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaWhite

@Composable
fun NovaApp(
    viewModel: NovaViewModel = viewModel()
) {
    val context = LocalContext.current

    var isWebEdition by remember { mutableStateOf(true) }

    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val rmsVolume by viewModel.rmsVolume.collectAsStateWithLifecycle()
    val statusText by viewModel.statusText.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val autoSpeak by viewModel.autoSpeak.collectAsStateWithLifecycle()
    val isVideoPlaying by viewModel.isVideoPlaying.collectAsStateWithLifecycle()
    val partialTranscript by viewModel.partialTranscript.collectAsStateWithLifecycle()
    val isLiveMode by viewModel.isLiveMode.collectAsStateWithLifecycle()
    val isMicMuted by viewModel.isMicMuted.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    var showControls by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val chatListState = rememberLazyListState()

    // Handle back button when in Live Mode
    BackHandler(enabled = isLiveMode) {
        viewModel.exitLiveMode()
    }

    // Auto-scroll chat to bottom when messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            chatListState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher for RECORD_AUDIO
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        } else {
            showPermissionDialog = true
        }
    }

    fun handleMicClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleListening()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun handleStartLiveMode() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.enterLiveMode()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Permission explanation dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = "Microphone Permission Required",
                    color = NovaWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Nova needs microphone access to listen to your voice commands and conduct live conversations. You can also type commands into the text input below.",
                    color = NovaSlateText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaCyan,
                        contentColor = NovaDeepSpace
                    )
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Dismiss", color = NovaSlateText)
                }
            },
            containerColor = NovaSurfaceDark,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isWebEdition) {
            Box(modifier = Modifier.fillMaxSize()) {
                WebEditionView(viewModel = viewModel)

                // Top mode switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NovaSurfaceDark.copy(alpha = 0.88f))
                            .border(1.dp, NovaCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .clickable { isWebEdition = false }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Switch to Native UI",
                            color = NovaCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            // 1. Full Screen Cinematic Looping Video Background
            VideoBackground(
                isPlaying = isVideoPlaying,
                overlayAlpha = 0.58f
            )

        // 2. Foreground UI
        if (!userSession.isSignedIn) {
            // Unauthenticated Sign-in Landing Screen
            NovaAuthScreen(
                onSignIn = { name, email ->
                    viewModel.signIn(name, email)
                },
                modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
            )
        } else if (isLiveMode) {
            // Dedicated Live Conversation Mode
            NovaLiveConversation(
                isListening = isListening,
                isSpeaking = isSpeaking,
                isMuted = isMicMuted,
                rmsVolume = rmsVolume,
                statusText = statusText,
                messages = messages,
                onToggleListening = { handleMicClick() },
                onToggleMute = { viewModel.toggleLiveMicMute() },
                onStopSpeaking = { viewModel.stopSpeaking() },
                onSendQuery = { query -> viewModel.submitQuery(query) },
                onCloseLiveMode = { viewModel.exitLiveMode() },
                modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
            )
        } else {
            // Standard Console Mode
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(NovaSurfaceDark.copy(alpha = 0.88f))
                        .border(1.dp, NovaCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NovaCyan.copy(alpha = 0.2f))
                                .border(1.dp, NovaCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { isWebEdition = true }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "NOVA 🌐",
                                color = NovaCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NovaEmerald)
                                )
                                Text(
                                    text = userSession.name,
                                    color = NovaWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (isSpeaking) "Speaking response..." else if (isListening) "Listening to voice..." else "Active & Ready",
                                color = if (isSpeaking || isListening) NovaCyan else NovaSlateText,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Start Live Conversation Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(NovaCyan)
                                .clickable { handleStartLiveMode() }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                .testTag("start_live_conversation_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Live",
                                    tint = NovaDeepSpace,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Live",
                                    color = NovaDeepSpace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Video Play/Pause icon button
                        IconButton(
                            onClick = { viewModel.toggleVideoPlayback() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("appbar_toggle_video")
                        ) {
                            Icon(
                                imageVector = if (isVideoPlaying) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Toggle Background Video",
                                tint = if (isVideoPlaying) NovaCyan else NovaSlateText,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Settings / Controls drawer toggle
                        IconButton(
                            onClick = { showControls = !showControls },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("appbar_toggle_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "System Settings",
                                tint = if (showControls) NovaCyan else NovaWhite,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Sign Out Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.signOut() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("appbar_sign_out_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign out",
                                    tint = NovaRose.copy(alpha = 0.9f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Sign out",
                                    color = NovaWhite.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Expandable Controls Panel
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        NovaControlsPanel(
                            autoSpeak = autoSpeak,
                            onAutoSpeakChange = { viewModel.setAutoSpeak(it) },
                            speechRate = speechRate,
                            onSpeechRateChange = { viewModel.setSpeechRate(it) },
                            isVideoPlaying = isVideoPlaying,
                            onToggleVideoPlayback = { viewModel.toggleVideoPlayback() },
                            onTestVoice = { viewModel.speakText("Nova voice system is online and ready.") },
                            onClearChat = { viewModel.clearChatHistory() },
                            onSignOut = { viewModel.signOut() }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Central Animated Voice Visualizer Orb
                NovaVoiceVisualizer(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    rmsVolume = rmsVolume,
                    statusText = if (partialTranscript.isNotBlank()) "Heard: \"$partialTranscript\"" else statusText,
                    onOrbClick = { handleMicClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick suggestion chips
                QuickSuggestionRow(
                    suggestions = suggestions,
                    onSuggestionClick = { selected ->
                        viewModel.submitQuery(selected)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Timeline
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    NovaChatTimeline(
                        messages = messages,
                        listState = chatListState,
                        onSpeakMessage = { text -> viewModel.speakText(text) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(NovaSurfaceDark.copy(alpha = 0.92f))
                        .border(1.dp, NovaCyan.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = "Speak or type, e.g. 'Sign out' or 'Open YouTube'",
                                color = NovaSlateText,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("query_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NovaWhite,
                            unfocusedTextColor = NovaWhite,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = NovaCyan
                        ),
                        maxLines = 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    viewModel.submitQuery(textInput)
                                    textInput = ""
                                }
                            }
                        )
                    )

                    // Mic action button in bottom bar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) NovaRose.copy(alpha = 0.2f)
                                else NovaCyan.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isListening) NovaRose else NovaCyan.copy(alpha = 0.4f),
                                CircleShape
                            )
                            .clickable { handleMicClick() }
                            .testTag("bottom_bar_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = if (isListening) NovaRose else NovaCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send action button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (textInput.isNotBlank()) NovaCyan else Color.White.copy(alpha = 0.1f)
                            )
                            .clickable(enabled = textInput.isNotBlank()) {
                                viewModel.submitQuery(textInput)
                                textInput = ""
                            }
                            .testTag("send_query_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank()) NovaDeepSpace else NovaSlateText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
}
