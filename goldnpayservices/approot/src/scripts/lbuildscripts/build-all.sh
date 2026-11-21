#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════
#  Build all GoldNPay Docker images
#  Usage:   ./build-all.sh [version] [--no-cache]
#  Example: ./build-all.sh 1.0.0
#  The version is resolved ONCE (highest existing tag across
#  all GoldNPay images + 1, or 1.0.0) and used for every image,
#  so one build-all run always produces matching tags:
#     goldnpay-core-server, goldnpay-ops-server, goldnpay-frontend
#  Each backend is built only when its Dockerfile and build script exist.
# ═══════════════════════════════════════════════════════
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=buildconstants.sh
source "$SCRIPT_DIR/buildconstants.sh"
# shellcheck source=version-utils.sh
source "$SCRIPT_DIR/version-utils.sh"

VERSION=""
EXTRA_ARGS=()
for arg in "$@"; do
    case "$arg" in
        --no-cache) EXTRA_ARGS+=(--no-cache) ;;
        -h|--help)  sed -n '2,11p' "$0"; exit 0 ;;
        *)          VERSION="$arg" ;;
    esac
done

if [ -n "$VERSION" ]; then
    validate_version "$VERSION"
else
    VERSION=$(next_version "$FRONTEND_REPO" "$CORE_REPO" "$OPS_REPO")
    echo "No version supplied — auto-resolved: ${VERSION}"
fi

echo "═══════════════════════════════════════"
echo " Building GoldNPay v${VERSION}"
echo "═══════════════════════════════════════"

BUILT=()

# Core first: Ops' tests use Core's schema script, and Core owns the database.
if [ -f "$CORE_HOME/$DOCKERFILE" ] && [ -x "$SCRIPT_DIR/build-backend-core.sh" ]; then
    "$SCRIPT_DIR/build-backend-core.sh" "$VERSION" "${EXTRA_ARGS[@]}"
    BUILT+=("${CORE_REPO}:${VERSION}")
    echo ""
else
    echo "Skipping Core (no $CORE_HOME/$DOCKERFILE or build-backend-core.sh)"
    echo ""
fi

if [ -f "$OPS_HOME/$DOCKERFILE" ] && [ -x "$SCRIPT_DIR/build-backend-ops.sh" ]; then
    "$SCRIPT_DIR/build-backend-ops.sh" "$VERSION" "${EXTRA_ARGS[@]}"
    BUILT+=("${OPS_REPO}:${VERSION}")
    echo ""
else
    echo "Skipping Ops (no $OPS_HOME/$DOCKERFILE or build-backend-ops.sh)"
    echo ""
fi

"$SCRIPT_DIR/build-frontend.sh" "$VERSION" "${EXTRA_ARGS[@]}"
BUILT+=("${FRONTEND_REPO}:${VERSION}")

echo ""
echo "═══════════════════════════════════════"
echo " All images built — v${VERSION}"
printf '   %s\n' "${BUILT[@]}"
echo "═══════════════════════════════════════"
docker images --filter "reference=goldnpay-*"
echo "═══════════════════════════════════════"
echo " To deploy: set CLIENT_IMAGE_TAG, CORE_IMAGE_TAG and OPS_IMAGE_TAG to ${VERSION} in docker/.env.<env>"
echo "            or run  scripts/lstartupscripts/<env>-app-up.sh ${VERSION}"
