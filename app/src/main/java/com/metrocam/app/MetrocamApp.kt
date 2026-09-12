package com.metrocam.app

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class MetrocamApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // OpenCV 4.9+ ships its native libraries inside the AAR itself, so initLocal()
        // loads them synchronously - no OpenCV Manager app or async callback needed.
        val loaded = OpenCVLoader.initLocal()
        Log.i(TAG, "OpenCV native libraries loaded: $loaded")
    }

    companion object {
        private const val TAG = "MetrocamApp"
    }
}
