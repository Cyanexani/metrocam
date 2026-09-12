package com.metrocam.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.metrocam.app.camera.CaptureMode

@Composable
fun ModeSelector(selected: CaptureMode, onSelect: (CaptureMode) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        CaptureMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Text(
                text = mode.label,
                color = if (isSelected) Color.Yellow else Color.White,
                modifier = Modifier
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(mode) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}
