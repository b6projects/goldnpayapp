#!/usr/bin/env bash
# DEV app stack: up   Usage: ./dev-app-up.sh [version]
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" dev app up "$@"
