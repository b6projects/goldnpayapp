#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════
#  Build the GoldNPay frontend Docker image
#  Usage:   ./build-frontend.sh [version] [--no-cache]
#  Example: ./build-frontend.sh 1.0.0
#  If version is omitted, it auto-increments from the latest
#  goldnpay-frontend image tag (starts at 1.0.0).
#  Tags produced:  goldnpay-frontend:<version>  and  goldnpay-frontend:latest
# ═══════════════════════════════════════════════════════
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=buildconstants.sh
source "$SCRIPT_DIR/buildconstants.sh"
# shellcheck source=version-utils.sh
source "$SCRIPT_DIR/version-utils.sh"

VERSION=""
NO_CACHE=()
for arg in "$@"; do
    case "$arg" in
        --no-cache) NO_CACHE=(--no-cache) ;;
        -h|--help)  sed -n '2,9p' "$0"; exit 0 ;;
        *)          VERSION="$arg" ;;
    esac
done

REPO_NAME="$FRONTEND_REPO"

echo "========================================"
echo " Starting ${REPO_NAME} build"
echo "========================================"

if [ -n "$VERSION" ]; then
    validate_version "$VERSION"
else
    VERSION=$(next_version "$REPO_NAME")
    echo "No version supplied — auto-resolved: ${VERSION}"
fi

IMAGE_NAME="${REPO_NAME}:${VERSION}"
check_version_exists "$IMAGE_NAME"

if [ ! -f "$FRONTEND_HOME/$DOCKERFILE" ]; then
    echo "ERROR: Dockerfile not found: $FRONTEND_HOME/$DOCKERFILE" >&2
    exit 1
fi

COMMIT=$(git_commit "$FRONTEND_HOME")

echo "========================================"
echo " Building  : ${IMAGE_NAME}"
echo " Context   : ${FRONTEND_HOME}"
echo " Commit    : ${COMMIT}"
echo "========================================"

docker build "${NO_CACHE[@]}" \
    -f "$FRONTEND_HOME/$DOCKERFILE" \
    --build-arg NODE_VERSION="$NODE_VERSION" \
    --build-arg APP_VERSION="$VERSION" \
    --build-arg GIT_COMMIT="$COMMIT" \
    --build-arg VITE_SITE_URL="${VITE_SITE_URL:-}" \
    -t "$IMAGE_NAME" \
    -t "${REPO_NAME}:latest" \
    "$FRONTEND_HOME"

echo ""
echo "${REPO_NAME} image built: ${IMAGE_NAME} (also tagged latest)"
echo "========================================"
