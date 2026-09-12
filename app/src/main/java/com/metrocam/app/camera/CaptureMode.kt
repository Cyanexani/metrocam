package com.metrocam.app.camera

// Windows Phone pivot titles are lowercase; acronyms (like "HDR", "JPG") are a
// documented exception and stay uppercase.
enum class CaptureMode(val label: String) {
    PHOTO("photo"),
    HDR("HDR"),
    NIGHT("night"),
}
