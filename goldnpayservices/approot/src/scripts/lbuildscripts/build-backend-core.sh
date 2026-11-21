#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════
#  Build the GoldNPay Core server Docker image
#  Usage:   ./build-backend-core.sh [version] [--no-cache]
#  Example: ./build-backend-core.sh 1.0.0
#  If version is omitted, it auto-increments from the latest
#  GoldNPay image tag (starts at 1.0.0).
#  Tags produced:  goldnpay-core-server:<version>  and  goldnpay-core-server:latest
#  The unit and integration tests run inside the image build.
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
        -h|--help)  sed -n '2,10p' "$0"; exit 0 ;;
        *)          VERSION="$arg" ;;
    esac
done

REPO_NAME="$CORE_REPO"

echo "========================================"
echo " Starting ${REPO_NAME} build"
echo "========================================"

if [ -n "$VERSION" ]; then
    validate_version "$VERSION"
else
    VERSION=$(next_version "$FRONTEND_REPO" "$CORE_REPO" "$OPS_REPO")
    echo "No version supplied — auto-resolved: ${VERSION}"
fi

IMAGE_NAME="${REPO_NAME}:${VERSION}"
check_version_exists "$IMAGE_NAME"

if [ ! -f "$CORE_HOME/$DOCKERFILE" ]; then
    echo "ERROR: Dockerfile not found: $CORE_HOME/$DOCKERFILE" >&2
    exit 1
fi

COMMIT=$(git_commit "$CORE_HOME")

echo "========================================"
echo " Building  : ${IMAGE_NAME}"
echo " Context   : ${CORE_HOME}"
echo " Commit    : ${COMMIT}"
echo "========================================"

docker build "${NO_CACHE[@]}" \
    -f "$CORE_HOME/$DOCKERFILE" \
    --build-arg JAVA_VERSION="$JAVA_VERSION" \
    --build-arg APP_VERSION="$VERSION" \
    --build-arg GIT_COMMIT="$COMMIT" \
    -t "$IMAGE_NAME" \
    -t "${REPO_NAME}:latest" \
    "$CORE_HOME"

echo ""
echo "${REPO_NAME} image built: ${IMAGE_NAME} (also tagged latest)"
echo "========================================"
