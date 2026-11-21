#!/usr/bin/env bash
# DEV db stack: up   Usage: ./dev-db-up.sh [version]
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" dev db up "$@"
