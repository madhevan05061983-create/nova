package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaDeepSpace
import com.example.ui.theme.NovaSlateText
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaWhite

@Composable
fun NovaAuthScreen(
    onSignIn: (name: String, email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSigningIn by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "auth_orb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Voice Orb Visualizer
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                NovaCyan.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(NovaDeepSpace.copy(alpha = 0.9f))
                    .border(2.dp, NovaCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = NovaCyan,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(NovaCyan.copy(alpha = 0.15f))
                .border(1.dp, NovaCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "NOVA VOICE AGENT",
                color = NovaCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Activate Nova Voice Agent",
            color = NovaWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Sign in to speak or type commands, open any app on your device, check battery status, and interact with Nova.",
            color = NovaSlateText,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Primary Google Sign-In Button
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(NovaCyan)
                .clickable(enabled = !isSigningIn) {
                    isSigningIn = true
                    onSignIn("Madhevan", "operator@nova.local")
                }
                .padding(vertical = 14.dp, horizontal = 20.dp)
                .testTag("google_sign_in_button"),
            contentAlignment = Alignment.Center
        ) {
            if (isSigningIn) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = NovaDeepSpace,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Signing in...",
                        color = NovaDeepSpace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GoogleIcon(modifier = Modifier.size(20.dp))

                    Text(
                        text = "Sign in with Google",
                        color = NovaDeepSpace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Instant Operator Access (No public gmail exposed)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(NovaSurfaceDark.copy(alpha = 0.8f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .clickable {
                    onSignIn("Operator", "operator@nova.local")
                }
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .testTag("demo_sign_in_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Quick Access as Operator",
                color = NovaWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Clean Google G Icon
 */
@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Blue right arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, 0f),
            size = size,
            style = Stroke(width = w * 0.22f)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, 0f),
            size = size,
            style = Stroke(width = w * 0.22f)
        )
        // Yellow left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, 0f),
            size = size,
            style = Stroke(width = w * 0.22f)
        )
        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, 0f),
            size = size,
            style = Stroke(width = w * 0.22f)
        )
        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.95f, h * 0.5f),
            strokeWidth = w * 0.22f
        )
    }
}
