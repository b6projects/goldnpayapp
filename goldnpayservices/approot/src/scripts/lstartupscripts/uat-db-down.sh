#!/usr/bin/env bash
# UAT db stack: down
set -euo pipefail
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/stack.sh" uat db down "$@"
