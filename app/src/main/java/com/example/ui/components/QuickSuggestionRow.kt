package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyan

@Composable
fun QuickSuggestionRow(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEachIndexed { index, suggestion ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NovaCyan.copy(alpha = 0.08f))
                    .border(1.dp, NovaCyan.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("suggestion_chip_$index")
            ) {
                Text(
                    text = suggestion,
                    color = NovaCyan,
                    fontSize = 12.sp
                )
            }
        }
    }
}
