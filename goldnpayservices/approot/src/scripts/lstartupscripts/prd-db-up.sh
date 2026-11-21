#!/usr/bin/env bash
# PRD db stack: up   Usage: ./prd-db-up.sh [version]
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" prd db up "$@"
