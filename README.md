# metrocam

A GCam-style computational-photography camera app for Android — manual (Pro-mode) controls
plus HDR and Night modes, built on:

- **[CameraX](https://developer.android.com/training/camerax)** (Camera2 under the hood) for
  preview and capture, with live manual controls (ISO, shutter speed, focus distance, white
  balance) via `Camera2CameraControl`.
- **[OpenCV](https://opencv.org/) (`org.opencv:opencv`, published directly to Maven Central)**
  for the computational-photography pipeline:
  - **HDR** — `AlignMTB` (bracket alignment) + `MergeMertens` (exposure fusion).
  - **Night mode** — `AlignMTB` + multi-frame averaging + `fastNlMeansDenoisingColored`.

Google's actual HDR+/Night Sight are proprietary; these are real, working equivalents built
from OpenCV's own algorithms, not a reproduction of Google's exact output.

## Project layout

```
app/src/main/java/com/metrocam/app/
├── MainActivity.kt          # permission gate + Compose entry point
├── MetrocamApp.kt           # loads OpenCV's native libs at process start
├── camera/                  # CameraX + Camera2 manual-control plumbing
├── processing/              # OpenCV-based HDR / Night mode pipelines
├── ui/                      # Compose UI (preview, mode selector, manual controls panel)
└── util/                    # MediaStore save helper
```

## Building

This repo has no local Android SDK dependency baked in — the intended build path is
**GitHub Actions** (`.github/workflows/build.yml`): every push to `main` and every PR builds
a debug APK and uploads it as a workflow artifact. Trigger it manually from the Actions tab
via "Run workflow" (`workflow_dispatch`) as well.

To build locally instead, open the project in **Android Studio** (it will fetch the SDK
components it needs automatically), or install the Android SDK yourself and run:

```bash
./gradlew assembleDebug
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Status

Early scaffold — first real verification happens on the first CI run. Expect to iterate on
build config as Gradle/AGP/OpenCV/CameraX version issues surface there.

### Known limitations / next steps

- No release signing config yet (`assembleRelease` needs a keystore + signing config in
  `app/build.gradle.kts` before it's usable for real distribution).
- HDR/Night processing runs on full-resolution frames — fine on modern devices, but not yet
  optimized for very high-megapixel sensors.
- No explicit RAW capture support yet.
