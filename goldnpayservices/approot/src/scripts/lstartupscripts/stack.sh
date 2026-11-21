#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════
#  Start / stop a GoldNPay stack
#  Usage:   ./stack.sh <dev|uat|prd> <app|db> <up|down|status|logs> [version]
#  Examples:
#     ./stack.sh prd app up            # runs CLIENT_IMAGE_TAG from .env.prd
#     ./stack.sh prd app up 1.0.3      # runs goldnpay-frontend:1.0.3
#     ./stack.sh uat db  up
#     ./stack.sh prd app logs
#  The <env>-<stack>-<action>.sh wrappers call this script.
# ═══════════════════════════════════════════════════════
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=constants.sh
source "$SCRIPT_DIR/constants.sh"

usage() { sed -n '2,10p' "$0"; exit 1; }

[ $# -ge 3 ] || usage
ENV_NAME="$(echo "$1" | tr '[:upper:]' '[:lower:]')"
STACK="$2"
ACTION="$3"
VERSION="${4:-}"

case "$ENV_NAME" in dev|uat|prd) ;; *) echo "ERROR: unknown env '$1'" >&2; usage ;; esac
case "$STACK" in
    app) COMPOSE_FILE="$APP_COMPOSE_FILE" ;;
    db)  COMPOSE_FILE="$DB_COMPOSE_FILE" ;;
    *)   echo "ERROR: unknown stack '$2'" >&2; usage ;;
esac
case "$ACTION" in up|down|status|logs) ;; *) echo "ERROR: unknown action '$3'" >&2; usage ;; esac

ENV_FILE=".env.${ENV_NAME}"
PROJECT_NAME="${PROJECT_PREFIX}-${ENV_NAME}-${STACK}"
ENV_UPPER="$(echo "$ENV_NAME" | tr '[:lower:]' '[:upper:]')"

# Prefer the Compose v2 plugin; fall back to legacy docker-compose.
if docker compose version > /dev/null 2>&1; then
    COMPOSE=(docker compose)
elif command -v docker-compose > /dev/null 2>&1; then
    COMPOSE=(docker-compose)
else
    echo "ERROR: neither 'docker compose' nor 'docker-compose' is available" >&2
    exit 1
fi

cd "$DOCKER_HOME"
[ -f "$ENV_FILE" ] || { echo "ERROR: $DOCKER_HOME/$ENV_FILE not found" >&2; exit 1; }

# Read KEY=value from the env file without executing it.
env_value() {
    grep -E "^[[:space:]]*$1=" "$ENV_FILE" | tail -1 | cut -d= -f2- \
        | sed -e 's/[[:space:]]*#.*$//' -e 's/^["'\'']//' -e 's/["'\'']$//' -e 's/[[:space:]]*$//'
}

NETWORK_NAME="$(env_value NETWORK_NAME)"
[ -n "$NETWORK_NAME" ] || { echo "ERROR: NETWORK_NAME missing in $ENV_FILE" >&2; exit 1; }

# A version argument overrides CLIENT_IMAGE_TAG / SERVER_IMAGE_TAG from the env file
# (shell variables take precedence over --env-file in Compose).
if [ -n "$VERSION" ]; then
    if [[ ! "$VERSION" =~ ^([0-9]+\.[0-9]+\.[0-9]+|latest)$ ]]; then
        echo "ERROR: '$VERSION' is not a valid version (e.g. 1.0.3 or latest)" >&2
        exit 1
    fi
    export CLIENT_IMAGE_TAG="$VERSION"
    export SERVER_IMAGE_TAG="$VERSION"
fi

compose() {
    "${COMPOSE[@]}" -p "$PROJECT_NAME" --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

ensure_network() {
    echo "######################## Network Check ###################################"
    if docker network inspect "$NETWORK_NAME" > /dev/null 2>&1; then
        echo "$NETWORK_NAME network already exists."
    else
        echo "$NETWORK_NAME network does not exist. Creating network..."
        docker network create "$NETWORK_NAME"
    fi
}

check_images() {
    local image missing=0
    while read -r image; do
        [ -z "$image" ] && continue
        if docker image inspect "$image" > /dev/null 2>&1; then
            echo "Image found: $image"
        else
            echo "Image NOT found locally: $image (Compose will try to pull it)"
            missing=1
        fi
    done < <(compose config --images)
    return $missing
}

ensure_volume_dirs() {
    local base db
    base="$(env_value PROJECT_VOLUME)"
    db="$(env_value DB_VOLUME)"
    if [ -n "$base" ] && [ -n "$db" ] && [ ! -d "$base/$db" ]; then
        echo "Creating data folder $base/$db"
        mkdir -p "$base/$db" || echo "WARNING: could not create $base/$db (create it with sudo)"
    fi
}

echo "######################## ${ENV_UPPER} ${STACK} : ${ACTION} ###################################"
echo "Docker folder : $DOCKER_HOME"
echo "Project       : $PROJECT_NAME"
echo "Env file      : $ENV_FILE"
echo "Compose file  : $COMPOSE_FILE"

case "$ACTION" in
    up)
        ensure_network
        [ "$STACK" = "db" ] && ensure_volume_dirs
        check_images || true
        compose up -d
        echo "######################## ${ENV_UPPER} ${STACK} startup complete ###################################"
        compose ps
        ;;
    down)
        compose down
        echo "######################## ${ENV_UPPER} ${STACK} stopped ###################################"
        docker ps --filter "label=com.docker.compose.project=${PROJECT_NAME}"
        ;;
    status)
        compose ps
        ;;
    logs)
        compose logs --tail=200 -f
        ;;
esac
