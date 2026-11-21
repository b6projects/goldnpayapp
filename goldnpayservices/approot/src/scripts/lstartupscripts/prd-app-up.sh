#!/usr/bin/env bash
# PRD app stack: up   Usage: ./prd-app-up.sh [version]
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" prd app up "$@"
