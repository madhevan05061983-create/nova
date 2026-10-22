package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanDark
import com.example.ui.theme.NovaCyanLight
import com.example.ui.theme.NovaDeepSpace
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaWhite

@Composable
fun NovaVoiceVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    rmsVolume: Float,
    statusText: String,
    onOrbClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nova_orb_transition")

    // Ambient pulsing animation
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    )

    // Speaking waveform bar animation heights
    val barHeight1 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barHeight1"
    )
    val barHeight2 by infiniteTransition.animateFloat(
        initialValue = 28f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barHeight2"
    )
    val barHeight3 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 46f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barHeight3"
    )

    val currentScale = when {
        isListening -> (1.0f + rmsVolume * 0.25f).coerceIn(1.0f, 1.35f)
        isSpeaking -> ambientPulse * 1.06f
        else -> ambientPulse
    }

    val orbBorderColor = when {
        isListening -> NovaEmerald
        isSpeaking -> NovaCyan
        else -> NovaCyan.copy(alpha = 0.45f)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Orb Center
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(currentScale),
            contentAlignment = Alignment.Center
        ) {
            // Outer Halo Ring
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (isListening) NovaEmerald.copy(alpha = 0.28f)
                                else if (isSpeaking) NovaCyan.copy(alpha = 0.35f)
                                else NovaCyan.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Interactive Orb Button
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (isListening) Color(0xFF064E3B)
                                else if (isSpeaking) Color(0xFF083344)
                                else NovaDeepSpace.copy(alpha = 0.85f),
                                NovaDeepSpace
                            )
                        )
                    )
                    .border(2.dp, orbBorderColor, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = NovaCyan),
                        onClick = onOrbClick
                    )
                    .testTag("nova_voice_orb"),
                contentAlignment = Alignment.Center
            ) {
                if (isSpeaking) {
                    // Reactive audio waveform bars when Nova is speaking aloud
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(barHeight1.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(NovaCyanLight)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(barHeight3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(NovaCyan)
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(barHeight2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(NovaWhite)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(barHeight3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(NovaCyan)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(barHeight1.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(NovaCyanDark)
                        )
                    }
                } else if (isListening) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Listening",
                        tint = NovaRose,
                        modifier = Modifier.size(38.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Tap to speak with Nova",
                        tint = NovaCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isListening) NovaEmerald.copy(alpha = 0.15f)
                    else if (isSpeaking) NovaCyan.copy(alpha = 0.15f)
                    else Color.White.copy(alpha = 0.08f)
                )
                .border(
                    1.dp,
                    if (isListening) NovaEmerald.copy(alpha = 0.3f)
                    else if (isSpeaking) NovaCyan.copy(alpha = 0.3f)
                    else Color.White.copy(alpha = 0.12f),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = when {
                    isListening -> "Listening... Speak now"
                    isSpeaking -> "Nova is speaking"
                    else -> "Tap Orb to Speak"
                },
                color = when {
                    isListening -> NovaEmerald
                    isSpeaking -> NovaCyan
                    else -> NovaWhite.copy(alpha = 0.85f)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = statusText,
            color = NovaWhite.copy(alpha = 0.65f),
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}
