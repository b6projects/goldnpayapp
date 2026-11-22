# ═══════════════════════════════════════════════════════
#  GoldNPay frontend image
#  Stage 1 builds the Vite/React app (tsc + vite build).
#  Stage 2 serves dist/ with a small Express server that
#  also serves /env-config.js from container env vars, so
#  one image runs in DEV, UAT and PRD.
#
#  Build:  docker build -t goldnpay-frontend:1.0.0 .
#          (normally via scripts/lbuildscripts/build-frontend.sh)
# ═══════════════════════════════════════════════════════
ARG NODE_VERSION=24.20.0

# ---------- Stage 1: build ----------
FROM node:${NODE_VERSION}-alpine AS build
WORKDIR /app

# Install dependencies first so this layer is cached
# until package.json / package-lock.json change.
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

# Copy the source (.dockerignore keeps node_modules, dist,
# coverage and .env files out) and build. The build also prerenders every
# page to static HTML; VITE_SITE_URL (the public origin, e.g. https://www.example.in)
# is baked into canonical URLs, the sitemap and robots.txt.
ARG VITE_SITE_URL=
ENV VITE_SITE_URL=${VITE_SITE_URL}
COPY . .
RUN npm run build && ls -la dist

# ---------- Stage 2: server dependencies ----------
FROM node:${NODE_VERSION}-alpine AS serverdeps
WORKDIR /app/server
COPY server/package.json server/package-lock.json ./
RUN npm ci --omit=dev --no-audit --no-fund

# ---------- Stage 3: runtime ----------
FROM node:${NODE_VERSION}-alpine AS runtime

ARG APP_VERSION=dev
ARG GIT_COMMIT=unknown
LABEL org.opencontainers.image.title="goldnpay-frontend" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"

ENV NODE_ENV=production \
    PORT=4173 \
    APP_VERSION=${APP_VERSION}

WORKDIR /app

# Only the built site, the server and its production deps.
COPY --from=build      --chown=node:node /app/dist                ./dist
COPY --from=serverdeps --chown=node:node /app/server/node_modules ./server/node_modules
COPY                   --chown=node:node server/package.json server/server.js ./server/

# Run as the unprivileged user that ships with the node image.
USER node

EXPOSE 4173

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${PORT}/health" > /dev/null || exit 1

CMD ["node", "server/server.js"]
