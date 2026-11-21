# GoldNPay — Docker build and run

Modelled on the GoldBucks setup (`GoldBucks-App/docker`, `scripts/lnbuildimages`, `scripts/lstartupscripts`).

## Layout

| Path (under `approot/src`) | What it is |
|---|---|
| `frontend/Dockerfile` | Multi-stage image: `npm ci` + `npm run build`, then a small runtime with `dist/` and the Express server. Non-root, health-checked. |
| `frontend/.dockerignore` | Keeps `node_modules`, `dist`, `coverage`, `.env*` out of the build. |
| `frontend/server/` | `server.js` (one server for all environments) and its own `package.json` / lockfile (Express only). |
| `backend/gpscoreserver/`, `backend/gpsopsserver/` | The Core and Ops Spring Boot servers, each with its own `Dockerfile` (multi-stage, tests run in the build, non-root, health-checked). |
| `docker/docker-compose-app.yml` | `gnpclient` (frontend), `gnpcoreserver` and `gnpopsserver`. |
| `docker/docker-compose-coredb.yml` | MySQL 8.4, bound to `127.0.0.1`, credentials from a secrets file outside git. |
| `docker/.env.dev` / `.env.uat` / `.env.prd` | Per-environment settings. **No secrets.** |
| `scripts/lbuildscripts/` | `build-all.sh`, `build-frontend.sh`, `build-backend-core.sh`, `build-backend-ops.sh`, `buildconstants.sh`, `version-utils.sh`. |
| `scripts/lstartupscripts/` | `stack.sh` plus wrappers `<env>-<app|db>-<up|down>.sh`. |

## Build

```bash
cd approot/src/scripts/lbuildscripts
./build-all.sh            # auto version: highest goldnpay-* tag + 1, or 1.0.0
./build-all.sh 1.2.0      # explicit version
./build-frontend.sh       # frontend only
./build-backend-core.sh   # Core server only
./build-backend-ops.sh    # Ops server only (needs BuildKit: it reads Core's schema script)
```

`build-all.sh` builds `goldnpay-core-server`, `goldnpay-ops-server` and `goldnpay-frontend` with one shared version, each also tagged `:latest`. A version that already exists is refused. The backend builds run the unit and integration tests, so a red test fails the image.

## Run

```bash
cd approot/src/scripts/lstartupscripts
./dev-app-up.sh           # DEV runs :latest            -> http://localhost:3060
./uat-app-up.sh 1.0.3     # run a specific version on UAT (port 3061)
./prd-app-up.sh           # runs CLIENT_IMAGE_TAG from .env.prd (port 3062)
./prd-app-down.sh
./stack.sh prd app status
./stack.sh prd app logs
```

The scripts create the Docker network named in the env file if it doesn't exist
(`goldnpay-net-dev`, `goldnpay-net-uat`, `goldnpay-net`).

| Env | Client port | Compose project | Network | Core / Ops port (host-only) | MySQL port (host-only) |
|---|---|---|---|---|---|
| DEV | 3060 | `gnp-dev-app` / `gnp-dev-db` | `goldnpay-net-dev` | 8220 / 8230 | 3316 |
| UAT | 3061 | `gnp-uat-app` / `gnp-uat-db` | `goldnpay-net-uat` | 8221 / 8231 | 3317 |
| PRD | 3062 | `gnp-prd-app` / `gnp-prd-db` | `goldnpay-net` | 8222 / 8232 | 3318 |

## Runtime config in the browser

`server.js` serves `/env-config.js`, which sets `window.env` from every container
variable whose name starts with `VITE_` (plus `VITE_APP_ENV` and `VITE_APP_VERSION`).
Compose passes `VITE_API_HOST` (from `API_HOST`) and `VITE_SITE_URL` (from `SITE_URL`).
These are public — never put secrets in a `VITE_*` variable.

The app doesn't read it yet. When it needs runtime values, add this line to
`frontend/index.html` before the main script, and read `window.env` in code:

```html
<script src="/env-config.js"></script>
```

`/health` returns `{"status":"UP","env":"PRD","version":"1.0.3"}`.

## Database

1. On the server, create the secrets file named by `DB_SECRETS_PATH`, e.g.
   `/opt/goldnpay/secrets/prd-db.env` (chmod 600):
   ```
   MYSQL_ROOT_PASSWORD=...
   MYSQL_USER=...
   MYSQL_PASSWORD=...
   ```
2. `./prd-db-up.sh` — data lives in `${PROJECT_VOLUME}/mysql` (`/opt/goldnpay/prd/mysql`).
3. Containers reach MySQL at host `gnp-prd-db`, port `3306`.
4. The schema is created by **Core's Flyway** on its first start. There is nothing to load by hand.

## Backend: Core and Ops

| Server | Image | Container | Host port (loopback) | Role |
|---|---|---|---|---|
| Core | `goldnpay-core-server` | `gnp-<env>-core` | 8220 / 8221 / 8222 | Users, auth, KYC, settings, gold rate, reports. The reverse proxy exposes its public routes (`/tc/v1/public`, `/auth`, `/client`, ...). Owns the database schema. |
| Ops | `goldnpay-ops-server` | `gnp-<env>-ops` | 8230 / 8231 / 8232 | Email, Telegram, Discord, Twitter, scheduled jobs. Called by Core only. **Never** route it through the proxy. |

Core calls Ops at `http://gnp-<env>-ops:8080` and Ops calls Core at `http://gnp-<env>-core:8080`, both on the Docker network.
Ops waits for Core to be **healthy** (not just started): Ops only validates the schema, so Core's migration must have run first.

**Secrets file** (`APP_SECRETS_PATH`, e.g. `/opt/goldnpay/secrets/prd-app.env`, chmod 600). Both servers read it, and each
refuses to start, naming the variable, if one it needs is missing:

```
# shared
JWT_SECRET_KEY=<256-bit key, Base64>           # same value for Core and Ops
CORE_TO_OPS_API_KEY=<32+ random bytes, Base64> # Core calls Ops
OPS_TO_CORE_API_KEY=<32+ random bytes, Base64> # Ops calls Core (a different value)
# Core
FIELD_ENCRYPTION_KEY=<32 bytes, Base64>        # AES-256 key for bank account numbers. Losing it loses the data.
# Ops
MAIL_HOST=...  MAIL_PORT=587  MAIL_USERNAME=...  MAIL_PASSWORD=...  MAIL_FROM=...  MAIL_REPLY_TO=...
TGM_SUPPORT_BOT_USERNAME=...  TGM_SUPPORT_BOT_TOKEN=...
TGM_PR_BOT_USERNAME=...       TGM_PR_BOT_TOKEN=...       TGM_PR_CHANNEL_ID=@channel-or-numeric-id
DISCORD_WEBHOOK_URL=...
TWITTER_API_KEY=...  TWITTER_API_SECRET=...  TWITTER_ACCESS_TOKEN=...  TWITTER_ACCESS_SECRET=...
```

Generate a key with `openssl rand -base64 32`. Rotate an API key by changing it in this file and restarting both servers.
Core and Ops share one `APP_SECRETS_PATH` file only for convenience; each variable is read by one or both as noted above.

**First admin.** Set `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` in the secrets file for the first start only. Core
creates an ADMIN when none exists. Remove both afterwards.

**Data folders.** `${PROJECT_VOLUME}/core` (KYC documents, images, logs; back it up) and `${PROJECT_VOLUME}/ops` (logs) are
mounted at `/app/server`. The containers run as uid `10001`; the startup script creates the folders and tries to `chown` them.

`ENV` (`DEV`/`UAT`/`PRD`) picks the Spring profile. Swagger UI is on in DEV and UAT and off in PRD.

## Windows notes

- Run the scripts from Git Bash or WSL. `approot/src/.gitattributes` keeps `*.sh` and
  `.env.*` on LF line endings; if a script fails with `$'\r'`, run `dos2unix` on it.
- With Docker Desktop, set `PROJECT_VOLUME` to a Windows path such as
  `C:/DockerVolumes/goldnpay/dev`.
