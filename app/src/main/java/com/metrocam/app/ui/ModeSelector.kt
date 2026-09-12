package com.metrocam.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrocam.app.camera.CaptureMode

/**
 * Windows Phone Pivot-style mode header: the active section is large and bright,
 * inactive ones are smaller and dimmed - no chip backgrounds, no rounded corners,
 * no borders. Tap a label to switch sections, the way a Pivot title bar works.
 */
@Composable
fun ModeSelector(selected: CaptureMode, onSelect: (CaptureMode) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
    ) {
        CaptureMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Text(
                text = mode.label,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
                fontSize = if (isSelected) 30.sp else 22.sp,
                modifier = Modifier.clickable { onSelect(mode) }
            )
        }
    }
}
