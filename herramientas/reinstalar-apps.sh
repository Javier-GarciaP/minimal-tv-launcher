#!/usr/bin/env bash
#
# Puts back the apps removed by limpiar-apps.sh.
#
# The apps were uninstalled for the current user only, so they can be reinstalled from the
# system image without downloading anything.
#
# Usage:  bash herramientas/reinstalar-apps.sh [serial]

set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
. "$HERE/common.sh"

SERIAL="${1:-${SERIAL:-}}"

RESTORE=(
    com.homwee.help
    com.homwee.mmp.fileexplorer
    com.homwee.multiscreenshare
    com.netflix.ninja
    com.iqiyi.i18n.tv
)

require_device

for package in "${RESTORE[@]}"; do
    result="$(sh cmd package install-existing "$package" || true)"
    case "$result" in
        *Package\ *installed*) echo "  restored  $package" ;;
        *) echo "  FAILED    $package ($result)" ;;
    esac
done

echo
echo "==> The apps on the home screen now:"
sh cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.LAUNCHER \
    | grep "packageName=" | sed 's/.*packageName=//' | sort -u | sed 's/^/  /'
