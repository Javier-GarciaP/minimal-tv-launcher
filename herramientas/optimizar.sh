#!/usr/bin/env bash
#
# Optimises the device for a snappier interface.
#
# It only touches reversible values, and it stores the original ones the first time it
# runs so that restaurar.sh can put everything back.
#
# Usage:  bash herramientas/optimizar.sh [serial]

set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
. "$HERE/common.sh"

SERIAL="${1:-${SERIAL:-}}"
BACKUP_DIR="$(cd "$HERE/.." && pwd)/backup"
VALUES_FILE="$BACKUP_DIR/valores-originales.txt"

require_device

mkdir -p "$BACKUP_DIR"

# Stores a setting the first time only, so re-running never overwrites the real original.
remember() {
    local key="$1"
    if ! grep -q "^${key}=" "$VALUES_FILE" 2>/dev/null; then
        local value
        value="$(sh settings get global "$key" || true)"
        printf '%s=%s\n' "$key" "$value" >> "$VALUES_FILE"
        echo "  saved  $key = $value"
    fi
}

echo "==> Guarding the original values in $VALUES_FILE"
for key in "${ANIMATION_SETTINGS[@]}"; do
    remember "$key"
done

echo "==> Turning the system animations off"
for key in "${ANIMATION_SETTINGS[@]}"; do
    sh settings put global "$key" 0.0
    echo "  $key = $(sh settings get global "$key")"
done

echo "==> Forcing the OpenGL renderer (the GPU has no Vulkan support)"
sh setprop debug.hwui.renderer skiagl
# The new processes read the property at start up, so the system UI has to be restarted.
sh am force-stop com.android.systemui || true

echo "==> Cleaning up background processes"
sh am kill-all || true

echo "==> Making the launcher the home screen"
sh cmd package set-home-activity "$LAUNCHER_ACTIVITY" >/dev/null
echo "  home = $(sh cmd package resolve-activity --brief -c android.intent.category.HOME -a android.intent.action.MAIN | tail -1)"

# The stock Google TV launcher and the setup wizard both register a home activity, and
# they win the resolution over the launcher the user picked. Disabling them for the
# current user is what makes the home button land on this launcher. The Google voice
# assistant lives in its own package (com.google.android.katniss) and is not affected.
echo "==> Disabling the stock launchers for this user"
for package in "$PREVIOUS_PACKAGE" "$SETUP_WIZARD_PACKAGE"; do
    sh pm disable-user --user 0 "$package" >/dev/null 2>&1 \
        && echo "  disabled $package" \
        || echo "  skipped  $package"
done
echo "  home = $(sh cmd package resolve-activity --brief -c android.intent.category.HOME -a android.intent.action.MAIN | tail -1)"

echo "==> Restricting the previous launcher to the background"
sh am set-inactive "$PREVIOUS_PACKAGE" true >/dev/null 2>&1 \
    && echo "  $PREVIOUS_PACKAGE set to inactive" \
    || echo "  skipped: this build does not allow it without root"

echo
echo "Done. Run 'bash herramientas/restaurar.sh' to undo everything."
