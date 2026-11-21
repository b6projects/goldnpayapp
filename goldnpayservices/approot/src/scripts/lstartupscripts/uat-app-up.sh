#!/usr/bin/env bash
# UAT app stack: up   Usage: ./uat-app-up.sh [version]
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" uat app up "$@"
