#!/usr/bin/env bash
#
# Removes the apps the user does not want from the home screen.
#
# The apps are uninstalled for the current user only (--user 0). That keeps /system
# untouched and can be undone with reinstalar-apps.sh.
#
# Usage:  bash herramientas/limpiar-apps.sh [serial]

set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
. "$HERE/common.sh"

SERIAL="${1:-${SERIAL:-}}"
BACKUP_DIR="$(cd "$HERE/.." && pwd)/backup"
PACKAGES_FILE="$BACKUP_DIR/paquetes-antes.txt"

# Apps to remove. None of them is a framework component: they are all regular apps with
# their own launcher activity. The user keeps Google Play, Xuper, YouTube, YouTube Music
# and the system media apps, so those are not touched.
REMOVE=(
    com.homwee.help                  # Ayuda
    com.homwee.mmp.fileexplorer      # Explorador de archivos
    com.homwee.multiscreenshare      # Compartir multipantalla
    com.netflix.ninja                # Netflix
    com.iqiyi.i18n.tv                # iQIYI
)

require_device

mkdir -p "$BACKUP_DIR"
sh pm list packages > "$PACKAGES_FILE"
echo "==> Saved the package list in $PACKAGES_FILE"

# The list is read once: piping it into "grep -q" would close the pipe early and make
# adb fail with SIGPIPE, which "set -o pipefail" would report as an error.
INSTALLED="$(sh pm list packages)"

for package in "${REMOVE[@]}"; do
    if grep -qx "package:${package}" <<< "$INSTALLED"; then
        result="$(sh pm uninstall --user 0 "$package" || true)"
        case "$result" in
            *Success*) echo "  removed  $package" ;;
            *) echo "  SKIPPED  $package ($result)" ;;
        esac
    else
        echo "  absent   $package"
    fi
done

echo
echo "==> The apps left on the home screen:"
sh cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.LAUNCHER \
    | grep "packageName=" | sed 's/.*packageName=//' | sort -u | sed 's/^/  /'
