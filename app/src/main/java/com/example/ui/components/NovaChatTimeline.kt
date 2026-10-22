package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MessageRole
import com.example.model.NovaMessage
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanDark
import com.example.ui.theme.NovaSlateText
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NovaChatTimeline(
    messages: List<NovaMessage>,
    listState: LazyListState,
    onSpeakMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageItem(
                message = message,
                onSpeak = { onSpeakMessage(message.content) },
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Nova Transcript", message.content))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                onOpenUrl = { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun MessageItem(
    message: NovaMessage,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val isUser = message.role == MessageRole.USER
    val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) 36.dp else 0.dp,
                end = if (isUser) 0.dp else 36.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isUser) Color(0x33FFFFFF)
                    else NovaSurfaceDark.copy(alpha = 0.88f)
                )
                .border(
                    1.dp,
                    if (isUser) Color.White.copy(alpha = 0.15f)
                    else NovaCyan.copy(alpha = 0.35f),
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                // Header: Sender tag and timestamp + action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isUser) Color.White.copy(alpha = 0.15f)
                                    else NovaCyan.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isUser) "YOU" else "NOVA",
                                color = if (isUser) NovaWhite else NovaCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = timeFormatted,
                            color = NovaSlateText,
                            fontSize = 10.sp
                        )
                    }

                    // Copy & Speak buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = NovaSlateText,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onCopy() }
                        )

                        if (!isUser) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak aloud",
                                tint = NovaCyan,
                                modifier = Modifier
                                    .size(17.dp)
                                    .clickable { onSpeak() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Message Text
                Text(
                    text = message.content,
                    color = NovaWhite,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Optional Action Button (e.g. Open YouTube / Google)
                if (!message.actionUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NovaCyanDark.copy(alpha = 0.4f))
                            .border(1.dp, NovaCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .clickable { onOpenUrl(message.actionUrl) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("action_url_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Link",
                            tint = NovaCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Launch ${message.actionLabel ?: "Link"}",
                            color = NovaCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
