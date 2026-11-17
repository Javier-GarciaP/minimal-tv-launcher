#!/usr/bin/env bash
#
# Builds the release APK and installs it on the device.
#
# Usage:  bash herramientas/instalar.sh [serial]

set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
ADB="${ADB:-adb}"
SERIAL="${1:-${SERIAL:-}}"

cd "$ROOT"

echo "==> Building the release APK"
./gradlew :app:assembleRelease

APK="$ROOT/app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK" ]; then
    echo "The APK was not produced. Check the Gradle output above." >&2
    exit 1
fi

if [ -n "$SERIAL" ]; then
    "$ADB" -s "$SERIAL" install -r "$APK"
else
    "$ADB" install -r "$APK"
fi

echo "==> Setting the launcher as the home screen"
if [ -n "$SERIAL" ]; then
    "$ADB" -s "$SERIAL" shell cmd package set-home-activity dev.minimaltv.launcher/.MainActivity
else
    "$ADB" shell cmd package set-home-activity dev.minimaltv.launcher/.MainActivity
fi

echo "Done."
