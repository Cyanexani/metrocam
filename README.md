# metrocam

A camera app for Android, built on a fork of **[Open Camera](https://opencamera.org.uk/)**
by Mark Harman, restyled with a Windows Phone / Metro design language (Lime accent, flat
black UI, Pivot-style navigation).

## Why fork Open Camera

Open Camera already implements the large majority of what a full-featured camera app
needs: manual (Pro-mode) controls, HDR, night mode, panorama, exposure/focus bracketing,
RAW (DNG) capture, burst mode, grid/level overlays, timer, multi-lens and front/rear
switching, and configurable video (resolution, FPS, stabilization). Rather than
re-implementing all of that from scratch, metrocam builds on it directly and focuses new
work on the UI/branding and the remaining gaps (see Roadmap below).

This repo's `git` history includes Open Camera's own history (merged in, not copied), so
upstream fixes can be pulled in later with `git fetch opencamera && git merge opencamera/master`.

## License

**GPL-3.0-or-later** (see [LICENSE](LICENSE)) - inherited from Open Camera. Any build of
this repo that's distributed must keep its full source available under GPL-3.0. This is a
one-way decision for the project: it can't go closed-source later without removing all
GPL-derived code.

## Building

Builds via **GitHub Actions** (`.github/workflows/build.yml`):
- Every push to `main` and every PR builds a debug APK, uploaded as a workflow artifact.
- Pushing a `v*` tag (e.g. `v0.0.1`) additionally builds a **signed release APK** and
  publishes it as a GitHub Release.

To build locally, open the project in Android Studio, or run:

```bash
./gradlew assembleDebug
```

## Status

Early fork - Open Camera's engine is in place; branding and Metro-style UI work are in
progress on top of it. The previous from-scratch Kotlin/Compose/CameraX/OpenCV prototype
is preserved on the `metrocam-native-v1` branch if that direction is revisited later.
