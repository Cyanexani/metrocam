# OpenCV's Java classes are bound to native code via JNI using exact class/method
# signatures generated at build time. R8/ProGuard must not rename or strip them.
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**
