package com.metrocam.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Mirrors the classic Windows Phone (Segoe WP) type ramp: large light-weight display
// text, small semibold letter-spaced labels for ALL CAPS titles, plain body text.
val Typography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Light, fontSize = 40.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Light, fontSize = 24.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.5.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp)
)
