# GoldNPay — Docker build and run

Modelled on the GoldBucks setup (`GoldBucks-App/docker`, `scripts/lnbuildimages`, `scripts/lstartupscripts`).

## Layout

| Path (under `approot/src`) | What it is |
|---|---|
| `frontend/Dockerfile` | Multi-stage image: `npm ci` + `npm run build`, then a small runtime with `dist/` and the Express server. Non-root, health-checked. |
| `frontend/.dockerignore` | Keeps `node_modules`, `dist`, `coverage`, `.env*` out of the build. |
| `frontend/server/` | `server.js` (one server for all environments) and its own `package.json` / lockfile (Express only). |
| `docker/docker-compose-app.yml` | `gnpclient` (frontend). A `gnpserver` (backend) block is ready but commented out. |
| `docker/docker-compose-coredb.yml` | MySQL 8.4, bound to `127.0.0.1`, credentials from a secrets file outside git. |
| `docker/.env.dev` / `.env.uat` / `.env.prd` | Per-environment settings. **No secrets.** |
| `scripts/lbuildscripts/` | `build-all.sh`, `build-frontend.sh`, `buildconstants.sh`, `version-utils.sh`. |
| `scripts/lstartupscripts/` | `stack.sh` plus wrappers `<env>-<app|db>-<up|down>.sh`. |

## Build

```bash
cd approot/src/scripts/lbuildscripts
./build-all.sh            # auto version: highest goldnpay-* tag + 1, or 1.0.0
./build-all.sh 1.2.0      # explicit version
./build-frontend.sh       # frontend only
```

Each build tags `goldnpay-frontend:<version>` and `goldnpay-frontend:latest`. A version that already exists is refused.

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

| Env | Client port | Compose project | Network | Backend port (later) | MySQL port (host-only) |
|---|---|---|---|---|---|
| DEV | 3060 | `gnp-dev-app` / `gnp-dev-db` | `goldnpay-net-dev` | 8220 | 3316 |
| UAT | 3061 | `gnp-uat-app` / `gnp-uat-db` | `goldnpay-net-uat` | 8221 | 3317 |
| PRD | 3062 | `gnp-prd-app` / `gnp-prd-db` | `goldnpay-net` | 8222 | 3318 |

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

## Database (when the backend lands)

1. On the server, create the secrets file named by `DB_SECRETS_PATH`, e.g.
   `/opt/goldnpay/secrets/prd-db.env` (chmod 600):
   ```
   MYSQL_ROOT_PASSWORD=...
   MYSQL_USER=...
   MYSQL_PASSWORD=...
   ```
2. `./prd-db-up.sh` — data lives in `${PROJECT_VOLUME}/mysql` (`/opt/goldnpay/prd/mysql`).
3. Containers reach MySQL at host `gnp-prd-db`, port `3306`.

## Adding the backend

1. Add `backend/Dockerfile` and `scripts/lbuildscripts/build-backend.sh`
   (same shape as `build-frontend.sh`, using `BACKEND_HOME` / `BACKEND_REPO`).
   `build-all.sh` picks it up automatically.
2. Uncomment `gnpserver` in `docker-compose-app.yml`, add `depends_on: [gnpserver]`
   to `gnpclient`, and set `API_HOST` in each env file.
3. Put app secrets in the file named by `APP_SECRETS_PATH`.

## Windows notes

- Run the scripts from Git Bash or WSL. `approot/src/.gitattributes` keeps `*.sh` and
  `.env.*` on LF line endings; if a script fails with `$'\r'`, run `dos2unix` on it.
- With Docker Desktop, set `PROJECT_VOLUME` to a Windows path such as
  `C:/DockerVolumes/goldnpay/dev`.
