#!/usr/bin/env bash
# PRD db stack: down
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" prd db down "$@"
