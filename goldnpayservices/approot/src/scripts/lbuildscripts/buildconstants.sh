#!/usr/bin/env bash
# shellcheck disable=SC2034  # sourced: variables are used by the build scripts
# ═══════════════════════════════════════════════════════
#  Build constants — sourced by the build scripts.
#  Paths are absolute, worked out from this file's location,
#  so the scripts can be run from any directory.
# ═══════════════════════════════════════════════════════

BUILD_SCRIPTS_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# approot/src
PROJECT_ROOT="$(cd "$BUILD_SCRIPTS_HOME/../.." && pwd)"
readonly PROJECT_ROOT

readonly FRONTEND_HOME="$PROJECT_ROOT/frontend"
readonly BACKEND_HOME="$PROJECT_ROOT/backend"
readonly DOCKERFILE="Dockerfile"

readonly FRONTEND_REPO="goldnpay-frontend"
readonly BACKEND_REPO="goldnpay-backend"

readonly NODE_VERSION="24.20.0"
