#!/usr/bin/env bash
# UAT app stack: down
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" uat app down "$@"
