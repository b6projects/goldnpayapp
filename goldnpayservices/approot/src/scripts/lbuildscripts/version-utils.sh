#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════
#  Version utilities — sourced by the build scripts.
#  Image tags are plain semver: MAJOR.MINOR.PATCH (e.g. 1.0.3).
# ═══════════════════════════════════════════════════════

BASE_VERSION="1.0.0"

# Exit with an error unless $1 looks like 1.2.3
validate_version() {
    if [[ ! "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        echo "ERROR: '$1' is not a valid version. Use MAJOR.MINOR.PATCH, e.g. 1.0.0" >&2
        exit 1
    fi
}

# Highest semver tag across one or more image repositories,
# or empty if none exist.   latest_version goldnpay-frontend goldnpay-backend
latest_version() {
    local repo
    for repo in "$@"; do
        docker images --format '{{.Tag}}' "$repo" 2>/dev/null || true
    done \
        | grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' \
        | sort -t. -k1,1n -k2,2n -k3,3n \
        | tail -1
}

# Increment the patch component:   bump_patch 1.2.3  ->  1.2.4
bump_patch() {
    local major minor patch
    IFS='.' read -r major minor patch <<< "$1"
    echo "${major}.${minor}.$((patch + 1))"
}

# Next version for the given repositories:
#   images exist -> highest + 1 (patch);   none -> BASE_VERSION
next_version() {
    local current
    current=$(latest_version "$@")
    if [ -z "$current" ]; then
        echo "$BASE_VERSION"
    else
        bump_patch "$current"
    fi
}

# Exit with an error if repository:tag already exists locally.
check_version_exists() {
    local image_name="$1"
    if docker image inspect "$image_name" > /dev/null 2>&1; then
        echo "" >&2
        echo "ERROR: Image already exists: ${image_name}" >&2
        echo "Use a different version or remove the existing image first:" >&2
        echo "  docker rmi ${image_name}" >&2
        exit 1
    fi
}

# Short git commit of the working tree, or "unknown".
git_commit() {
    git -C "$1" rev-parse --short HEAD 2>/dev/null || echo "unknown"
}
