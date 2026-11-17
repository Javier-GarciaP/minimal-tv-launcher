#!/usr/bin/env bash
#
# Shared helpers for the device scripts.
#
# Every script works on a single device, selected with SERIAL or the first device
# reported by adb. Run with --help on any of them for the exact usage.

set -euo pipefail

ADB="${ADB:-adb}"
SERIAL="${SERIAL:-}"

# Package and activity that become the home screen.
LAUNCHER_PACKAGE="dev.minimaltv.launcher"
LAUNCHER_ACTIVITY="dev.minimaltv.launcher/.MainActivity"
PREVIOUS_HOME="com.google.android.apps.tv.launcherx/home.HomeActivity"
PREVIOUS_PACKAGE="com.google.android.apps.tv.launcherx"

# The setup wizard registers a recovery activity as a home candidate too. It has to be
# disabled for this user as well, otherwise it takes the home button back.
SETUP_WIZARD_PACKAGE="com.google.android.tungsten.setupwraith"

# Settings touched by the optimisation.
ANIMATION_SETTINGS=(
    window_animation_scale
    transition_animation_scale
    animator_duration_scale
)

# shell() runs a command on the device, with the CRLF line endings removed.
sh() {
    if [ -n "$SERIAL" ]; then
        "$ADB" -s "$SERIAL" shell "$@" | tr -d '\r'
    else
        "$ADB" shell "$@" | tr -d '\r'
    fi
}

require_device() {
    if [ -n "$SERIAL" ]; then
        if ! "$ADB" -s "$SERIAL" get-state >/dev/null 2>&1; then
            echo "No device with serial '$SERIAL'. Check the connection with 'adb devices'." >&2
            exit 1
        fi
    else
        if [ -z "$("$ADB" devices | awk 'NR>1 && $2=="device" {print $1}')" ]; then
            echo "No device connected. Check 'adb devices'." >&2
            exit 1
        fi
    fi
}
