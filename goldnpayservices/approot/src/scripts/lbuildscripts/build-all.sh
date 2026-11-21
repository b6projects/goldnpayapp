#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════
#  Build all GoldNPay Docker images
#  Usage:   ./build-all.sh [version] [--no-cache]
#  Example: ./build-all.sh 1.0.0
#  The version is resolved ONCE (highest existing tag across
#  all GoldNPay images + 1, or 1.0.0) and used for every image,
#  so one build-all run always produces matching tags.
#  The backend is built only when backend/Dockerfile and
#  build-backend.sh exist.
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
    VERSION=$(next_version "$FRONTEND_REPO" "$BACKEND_REPO")
    echo "No version supplied — auto-resolved: ${VERSION}"
fi

echo "═══════════════════════════════════════"
echo " Building GoldNPay v${VERSION}"
echo "═══════════════════════════════════════"

BUILT=()

if [ -f "$BACKEND_HOME/$DOCKERFILE" ] && [ -x "$SCRIPT_DIR/build-backend.sh" ]; then
    "$SCRIPT_DIR/build-backend.sh" "$VERSION" "${EXTRA_ARGS[@]}"
    BUILT+=("${BACKEND_REPO}:${VERSION}")
    echo ""
else
    echo "Skipping backend (no backend/$DOCKERFILE or build-backend.sh yet)"
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
echo " To deploy: set CLIENT_IMAGE_TAG=${VERSION} in docker/.env.<env>"
echo "            or run  scripts/lstartupscripts/<env>-app-up.sh ${VERSION}"
