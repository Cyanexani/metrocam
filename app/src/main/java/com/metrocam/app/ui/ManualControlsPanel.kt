package com.metrocam.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrocam.app.camera.ManualControlState
import com.metrocam.app.camera.SensorInfo
import com.metrocam.app.ui.theme.MetroLime
import com.metrocam.app.ui.theme.MetroPanel

@Composable
fun ManualControlsPanel(
    sensorInfo: SensorInfo?,
    state: ManualControlState,
    onStateChange: (ManualControlState) -> Unit,
    modifier: Modifier = Modifier
) {
    val isoRange = sensorInfo?.isoRange
    val exposureRange = sensorInfo?.exposureTimeRangeNs

    Column(modifier = modifier.fillMaxWidth()) {
        // The single-pixel accent rule Metro puts under every section header.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MetroLime)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MetroPanel)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "manual",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Switch(
                    checked = state.enabled,
                    onCheckedChange = { onStateChange(state.copy(enabled = it)) }
                )
            }

            if (state.enabled) {
                LabeledSlider(
                    label = "iso ${state.iso}",
                    value = state.iso.toFloat(),
                    range = (isoRange?.lower?.toFloat() ?: 50f)..(isoRange?.upper?.toFloat() ?: 3200f),
                    onChange = { onStateChange(state.copy(iso = it.toInt())) }
                )

                val shutterMs = state.exposureTimeNs / 1_000_000f
                LabeledSlider(
                    label = "shutter ${"%.1f".format(shutterMs)} ms",
                    value = state.exposureTimeNs.toFloat(),
                    range = (exposureRange?.lower?.toFloat() ?: 1_000_000f)..
                        (exposureRange?.upper?.toFloat() ?: 200_000_000f),
                    onChange = { onStateChange(state.copy(exposureTimeNs = it.toLong())) }
                )

                LabeledSlider(
                    label = "focus ${"%.2f".format(state.focusDistanceDiopters)} d",
                    value = state.focusDistanceDiopters,
                    range = 0f..(sensorInfo?.minFocusDistanceDiopters?.coerceAtLeast(0.1f) ?: 10f),
                    onChange = { onStateChange(state.copy(focusDistanceDiopters = it)) }
                )
            }
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            valueRange = range,
            onValueChange = onChange
        )
    }
}
