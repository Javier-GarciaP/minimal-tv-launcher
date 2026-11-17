#!/usr/bin/env bash
#
# Puts the device back the way it was before optimizar.sh ran.
#
# It can be run as many times as needed: the original values are read from
# backup/valores-originales.txt, and the home screen always goes back to the launcher
# that was installed before.
#
# Usage:  bash herramientas/restaurar.sh [serial]

set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
. "$HERE/common.sh"

SERIAL="${1:-${SERIAL:-}}"
BACKUP_DIR="$(cd "$HERE/.." && pwd)/backup"
VALUES_FILE="$BACKUP_DIR/valores-originales.txt"

require_device

if [ -f "$VALUES_FILE" ]; then
    echo "==> Restoring the original settings"
    while IFS='=' read -r key value; do
        [ -z "$key" ] && continue
        case "$key" in \#*) continue ;; esac
        # A setting that was "null" means it was never set: it has to be deleted.
        if [ "$value" = "null" ] || [ -z "$value" ]; then
            sh settings delete global "$key" >/dev/null
            echo "  $key = (deleted)"
        else
            # adb shell reads stdin, which would swallow the rest of the backup file.
            sh settings put global "$key" "$value" </dev/null
            echo "  $key = $(sh settings get global "$key" </dev/null)"
        fi
    done < "$VALUES_FILE"
else
    echo "==> No backup found, nothing to restore from"
fi

echo "==> Removing the forced renderer"
sh "setprop debug.hwui.renderer ''" >/dev/null 2>&1 || true

echo "==> Enabling the stock launchers again"
for package in "$PREVIOUS_PACKAGE" "$SETUP_WIZARD_PACKAGE"; do
    sh pm enable --user 0 "$package" >/dev/null 2>&1 \
        && echo "  enabled $package" \
        || echo "  skipped  $package"
done

echo "==> Giving the previous launcher back its foreground role"
sh am set-inactive "$PREVIOUS_PACKAGE" false >/dev/null 2>&1 || true
sh cmd package set-home-activity "$PREVIOUS_HOME" >/dev/null
echo "  home = $(sh cmd package resolve-activity --brief -c android.intent.category.HOME -a android.intent.action.MAIN | tail -1)"

echo
echo "Done. The device is back to its original state."
