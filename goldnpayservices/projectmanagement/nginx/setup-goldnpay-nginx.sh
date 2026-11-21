#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════
#  Installs the GoldNPay nginx site configs (goldnpay.in, uat.goldnpay.in).
#
#  Usage (on the server, as root):
#    sudo ./setup-goldnpay-nginx.sh [--ssl] [--dry-run]
#
#  Safety:
#   - Only creates/replaces the two GoldNPay site files and their symlinks.
#     mygoldbucks.com / test.mygoldbucks.com and all other sites are never edited.
#   - Aborts if any GoldNPay upstream port is already used by another nginx site
#     or is a port of an existing GoldBucks config.
#   - Runs `nginx -t` before reloading; on failure the previous state is restored
#     and nginx is NOT reloaded.
#   --ssl      afterwards run certbot (nginx plugin) for the GoldNPay domains only.
#   --dry-run  run the checks only; change nothing.
# ═══════════════════════════════════════════════════════
set -euo pipefail

SRC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AVAILABLE=/etc/nginx/sites-available
ENABLED=/etc/nginx/sites-enabled
SITES=(goldnpay.in uat.goldnpay.in)
CERTBOT_DOMAINS=(-d goldnpay.in -d www.goldnpay.in -d uat.goldnpay.in)
BACKUP_DIR="/etc/nginx/backup-goldnpay-$(date +%Y%m%d-%H%M%S)"

DO_SSL=0; DRY=0
for a in "$@"; do
  case "$a" in
    --ssl) DO_SSL=1 ;;
    --dry-run) DRY=1 ;;
    *) echo "Unknown option: $a" >&2; exit 2 ;;
  esac
done

[ "$(id -u)" -eq 0 ] || { echo "Run as root (sudo)." >&2; exit 1; }
command -v nginx >/dev/null || { echo "nginx not installed." >&2; exit 1; }
[ -d "$AVAILABLE" ] && [ -d "$ENABLED" ] || { echo "$AVAILABLE / $ENABLED not found (non-Debian layout?)." >&2; exit 1; }
for s in "${SITES[@]}"; do [ -f "$SRC_DIR/$s" ] || { echo "Missing $SRC_DIR/$s" >&2; exit 1; }; done

# Ports GoldNPay will proxy to (from the files being installed)
mapfile -t OUR_PORTS < <(grep -hoE 'proxy_pass +http://[^:]+:[0-9]+' "$SRC_DIR"/goldnpay.in "$SRC_DIR"/uat.goldnpay.in | grep -oE '[0-9]+$' | sort -u)
echo "GoldNPay upstream ports: ${OUR_PORTS[*]}"

# 1) Same port twice inside our own files for different upstreams is a bug
dups=$(grep -hoE 'proxy_pass +http://[^:]+:[0-9]+' "$SRC_DIR"/goldnpay.in "$SRC_DIR"/uat.goldnpay.in | grep -oE '[0-9]+$' | sort | uniq -d || true)
[ -z "$dups" ] || { echo "ERROR: port reused within GoldNPay configs: $dups" >&2; exit 1; }

# 2) Overlap with every other nginx site (GoldBucks included), ignoring our own files
conflict=0
for f in "$ENABLED"/* "$AVAILABLE"/* /etc/nginx/conf.d/*.conf; do
  [ -f "$f" ] || continue
  base=$(basename "$f")
  case "$base" in goldnpay.in|uat.goldnpay.in) continue ;; esac
  for p in "${OUR_PORTS[@]}"; do
    if grep -qE "(proxy_pass[^;]*:${p}\b|listen[^;]*\b${p}\b)" "$f"; then
      echo "CONFLICT: port $p is already used in $f" >&2; conflict=1
    fi
  done
done
[ "$conflict" -eq 0 ] || { echo "Aborting: resolve the port overlap first." >&2; exit 1; }

# 3) Warn (not fail) if something non-docker already listens on a port; the stacks may simply be up already.
if command -v ss >/dev/null; then
  for p in "${OUR_PORTS[@]}"; do
    if ss -ltnH "sport = :$p" | grep -q .; then echo "note: port $p is listening now (expected if the GoldNPay stack is running)."; fi
  done
fi

# 4) Domain already claimed by another site?
for d in goldnpay.in www.goldnpay.in uat.goldnpay.in; do
  for f in "$ENABLED"/*; do
    [ -f "$f" ] || continue
    case "$(basename "$f")" in goldnpay.in|uat.goldnpay.in) continue ;; esac
    if grep -qE "server_name[^;]*[[:space:]]${d}[[:space:];]" "$f"; then
      echo "CONFLICT: $d already served by $f" >&2; exit 1
    fi
  done
done

echo "Checks passed: no overlap with existing sites."
[ "$DRY" -eq 0 ] || { echo "--dry-run: nothing changed."; exit 0; }

# ── Install with rollback ──
mkdir -p "$BACKUP_DIR" /var/www/html
installed=()
rollback() {
  echo "Rolling back GoldNPay changes..." >&2
  for s in "${installed[@]}"; do
    rm -f "$ENABLED/$s"
    if [ -f "$BACKUP_DIR/$s" ]; then cp -a "$BACKUP_DIR/$s" "$AVAILABLE/$s"; ln -sf "$AVAILABLE/$s" "$ENABLED/$s"; else rm -f "$AVAILABLE/$s"; fi
  done
}

for s in "${SITES[@]}"; do
  [ ! -f "$AVAILABLE/$s" ] || cp -a "$AVAILABLE/$s" "$BACKUP_DIR/$s"
  install -m 644 "$SRC_DIR/$s" "$AVAILABLE/$s"
  ln -sf "$AVAILABLE/$s" "$ENABLED/$s"
  installed+=("$s")
done

if ! nginx -t; then
  rollback
  echo "nginx -t failed; nothing reloaded. Existing sites untouched." >&2
  exit 1
fi

systemctl reload nginx
echo "nginx reloaded with GoldNPay sites. Backup: $BACKUP_DIR"

if [ "$DO_SSL" -eq 1 ]; then
  command -v certbot >/dev/null || { echo "certbot not installed; skipping --ssl." >&2; exit 1; }
  # DNS for the three names must already point at this server.
  certbot --nginx "${CERTBOT_DOMAINS[@]}" --redirect
  nginx -t && systemctl reload nginx
fi

echo "Done. Verify: curl -I http://goldnpay.in/  and  curl -I http://uat.goldnpay.in/tc/v1/public/"
