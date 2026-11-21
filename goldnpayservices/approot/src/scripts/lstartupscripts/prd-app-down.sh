#!/usr/bin/env bash
# PRD app stack: down
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" prd app down "$@"
