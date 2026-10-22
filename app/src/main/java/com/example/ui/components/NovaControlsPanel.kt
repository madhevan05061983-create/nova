package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaSlateText
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaWhite

@Composable
fun NovaControlsPanel(
    autoSpeak: Boolean,
    onAutoSpeakChange: (Boolean) -> Unit,
    speechRate: Float,
    onSpeechRateChange: (Float) -> Unit,
    isVideoPlaying: Boolean,
    onToggleVideoPlayback: () -> Unit,
    onTestVoice: () -> Unit,
    onClearChat: () -> Unit,
    onSignOut: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NovaSurfaceDark.copy(alpha = 0.85f))
            .border(1.dp, NovaCyan.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYSTEM CONTROLS",
                color = NovaCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            // Clear chat icon button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClearChat() }
                    .padding(4.dp)
                    .testTag("clear_chat_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear History",
                    tint = NovaRose.copy(alpha = 0.8f),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Clear History",
                    color = NovaRose.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
        }

        // 1. Auto-Speak responses toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = NovaCyan,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Auto-Speak Audio",
                        color = NovaWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Read Nova responses aloud",
                        color = NovaSlateText,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = autoSpeak,
                onCheckedChange = onAutoSpeakChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NovaWhite,
                    checkedTrackColor = NovaCyan,
                    uncheckedThumbColor = NovaSlateText,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.testTag("auto_speak_switch")
            )
        }

        // 2. Video Background Playback Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = NovaCyan,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Cinematic Video Background",
                        color = NovaWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isVideoPlaying) "Background video looping" else "Video paused",
                        color = NovaSlateText,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isVideoPlaying) NovaCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f))
                    .border(1.dp, if (isVideoPlaying) NovaCyan else Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { onToggleVideoPlayback() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("toggle_video_bg_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isVideoPlaying) "Pause Video" else "Play Video",
                        tint = if (isVideoPlaying) NovaCyan else NovaWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isVideoPlaying) "Pause" else "Play",
                        color = if (isVideoPlaying) NovaCyan else NovaWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 3. Speaking Speed chips & Test Voice
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = NovaCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Speaking Speed",
                        color = NovaWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "${speechRate}x",
                    color = NovaCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.8f, 1.0f, 1.2f, 1.4f).forEach { rate ->
                    val isSelected = speechRate == rate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) NovaCyan else Color.White.copy(alpha = 0.08f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) NovaCyan else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSpeechRateChange(rate) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${rate}x",
                            color = if (isSelected) Color(0xFF020617) else NovaWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Test voice button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .clickable { onTestVoice() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("test_voice_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Test",
                        color = NovaCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (onSignOut != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NovaRose.copy(alpha = 0.15f))
                    .border(1.dp, NovaRose.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .clickable { onSignOut() }
                    .padding(vertical = 10.dp)
                    .testTag("panel_sign_out_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign out of Nova",
                    color = NovaRose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
