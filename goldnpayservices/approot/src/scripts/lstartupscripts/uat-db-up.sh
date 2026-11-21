#!/usr/bin/env bash
# UAT db stack: up   Usage: ./uat-db-up.sh [version]
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" uat db up "$@"
