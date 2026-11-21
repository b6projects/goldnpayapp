#!/usr/bin/env bash
# DEV db stack: down
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" dev db down "$@"
