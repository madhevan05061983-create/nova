package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MessageRole
import com.example.model.NovaMessage
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaDeepSpace
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaSlateText
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaWhite

@Composable
fun NovaLiveConversation(
    isListening: Boolean,
    isSpeaking: Boolean,
    isMuted: Boolean,
    rmsVolume: Float,
    statusText: String,
    messages: List<NovaMessage>,
    onToggleListening: () -> Unit,
    onToggleMute: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSendQuery: (String) -> Unit,
    onCloseLiveMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val streamListState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            streamListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Live Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NovaSurfaceDark.copy(alpha = 0.9f))
                .border(1.dp, NovaCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    Text(
                        text = "NOVA LIVE",
                        color = NovaCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Real-Time Voice Stream",
                        color = NovaWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mic Mute / Unmute Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isMuted) NovaRose.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                        .border(1.dp, if (isMuted) NovaRose else Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { onToggleMute() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("live_mic_mute_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isMuted) "Unmute Mic" else "Mute Mic",
                            tint = if (isMuted) NovaRose else NovaWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isMuted) "Muted" else "Mic On",
                            color = if (isMuted) NovaRose else NovaWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Exit Live Mode Button
                IconButton(
                    onClick = onCloseLiveMode,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("exit_live_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Live Mode",
                        tint = NovaSlateText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Central Live Visualizer Orb & Status
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NovaVoiceVisualizer(
                isListening = isListening,
                isSpeaking = isSpeaking,
                rmsVolume = rmsVolume,
                statusText = if (isSpeaking) "Nova is speaking to you..." else if (isListening) "Listening... Speak anytime to chat" else statusText,
                onOrbClick = { onToggleListening() },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (isSpeaking) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NovaRose.copy(alpha = 0.2f))
                        .border(1.dp, NovaRose.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onStopSpeaking() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("live_interrupt_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Interrupt",
                            tint = NovaRose,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Interrupt Nova",
                            color = NovaRose,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Live Transcript Stream Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(NovaSurfaceDark.copy(alpha = 0.85f))
                .border(1.dp, NovaCyan.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "LIVE TRANSCRIPT STREAM",
                    color = NovaCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyColumn(
                    state = streamListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages.takeLast(8), key = { it.id }) { msg ->
                        val isUser = msg.role == MessageRole.USER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isUser) "You:" else "Nova:",
                                color = if (isUser) NovaWhite else NovaCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = msg.content,
                                color = if (isUser) NovaSlateText else NovaWhite,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Quick Verbal Prompt Chips
        QuickSuggestionRow(
            suggestions = listOf("What time is it?", "Open YouTube", "Tell me a joke", "Security overview", "What can you do?"),
            onSuggestionClick = { onSendQuery(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Live Mode Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NovaSurfaceDark.copy(alpha = 0.95f))
                .border(1.dp, NovaCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = "Or speak / type question here...",
                        color = NovaSlateText,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("live_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NovaWhite,
                    unfocusedTextColor = NovaWhite,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = NovaCyan
                ),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textInput.isNotBlank()) {
                            onSendQuery(textInput)
                            textInput = ""
                        }
                    }
                )
            )

            // Direct Orb / Mic Toggle Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isListening) NovaRose.copy(alpha = 0.25f) else NovaCyan.copy(alpha = 0.2f))
                .border(1.dp, if (isListening) NovaRose else NovaCyan, CircleShape)
                .clickable { onToggleListening() }
                .testTag("live_mic_action_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = if (isListening) NovaRose else NovaCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Direct Send Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (textInput.isNotBlank()) NovaCyan else Color.White.copy(alpha = 0.1f))
                    .clickable(enabled = textInput.isNotBlank()) {
                        onSendQuery(textInput)
                        textInput = ""
                    }
                    .testTag("live_send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (textInput.isNotBlank()) NovaDeepSpace else NovaSlateText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
