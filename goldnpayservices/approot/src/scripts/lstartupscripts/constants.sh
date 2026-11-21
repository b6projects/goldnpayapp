#!/usr/bin/env bash
# shellcheck disable=SC2034  # sourced: variables are used by stack.sh
# ═══════════════════════════════════════════════════════
#  Startup constants — sourced by stack.sh.
#  Absolute paths from this file's location, so the scripts
#  work from any directory.
# ═══════════════════════════════════════════════════════

STARTUPSCRIPTS_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly STARTUPSCRIPTS_HOME
DOCKER_HOME="$(cd "$STARTUPSCRIPTS_HOME/../../docker" && pwd)"
readonly DOCKER_HOME

readonly APP_COMPOSE_FILE="docker-compose-app.yml"
readonly DB_COMPOSE_FILE="docker-compose-coredb.yml"

# Compose project names: gnp-<env>-app / gnp-<env>-db
readonly PROJECT_PREFIX="gnp"
