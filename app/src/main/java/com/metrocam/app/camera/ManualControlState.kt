package com.metrocam.app.camera

import android.hardware.camera2.CaptureRequest

/**
 * Manual (Camera2 "Pro mode") capture parameters. When [enabled] is false, the camera
 * is left in full auto (AE/AF/AWB) and the other fields are ignored.
 */
data class ManualControlState(
    val enabled: Boolean = false,
    val iso: Int = 100,
    val exposureTimeNs: Long = 8_000_000L, // ~1/125s
    val focusDistanceDiopters: Float = 0f, // 0 = focused at infinity
    val whiteBalanceMode: Int = CaptureRequest.CONTROL_AWB_MODE_AUTO
)
