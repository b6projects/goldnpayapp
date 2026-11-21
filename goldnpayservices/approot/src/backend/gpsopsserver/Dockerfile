# ═══════════════════════════════════════════════════════
#  GoldNPay Ops Server image (com.gps.os)
#  Stage 1 builds the jar with the Maven wrapper; the tests run during
#  the build. The Ops tests load Core's schema script (Core owns the
#  schema), which is supplied as the BuildKit named context "core":
#     docker build --build-context core=../gpscoreserver -t goldnpay-ops-server:1.0.0 .
#  (normally via scripts/lbuildscripts/build-backend-ops.sh)
#  Stage 2 is a small JRE runtime, non-root, health-checked.
# ═══════════════════════════════════════════════════════
ARG JAVA_VERSION=25

# ---------- Stage 1: build ----------
FROM eclipse-temurin:${JAVA_VERSION}-jdk-alpine AS build
WORKDIR /build/gpsopsserver

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

# Core's schema, at the path the tests expect (../gpscoreserver/src/main/resources/db/migration).
COPY --from=core src/main/resources/db/migration /build/gpscoreserver/src/main/resources/db/migration
COPY lombok.config ./
COPY src src
RUN ./mvnw -B -ntp package

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

ARG APP_VERSION=dev
ARG GIT_COMMIT=unknown
LABEL org.opencontainers.image.title="goldnpay-ops-server" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"

# Ops draws the gold rate onto images (ImageOverlayService); that needs fonts at runtime.
RUN apk add --no-cache fontconfig ttf-dejavu \
    && addgroup -S -g 10001 gnp && adduser -S -u 10001 -G gnp gnp \
    && mkdir -p /app/server/serverlogs && chown -R gnp:gnp /app
WORKDIR /app
COPY --from=build --chown=gnp:gnp /build/gpsopsserver/target/ops-server.jar /app/ops-server.jar

USER gnp
ENV SERVER_PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=UTC -Djava.awt.headless=true"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD wget -qO- "http://127.0.0.1:${SERVER_PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

# ENV (DEV/UAT/PRD) selects the Spring profile: dev, uat or prd.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dspring.profiles.active=$(echo \"${ENV:-dev}\" | tr 'A-Z' 'a-z') -jar /app/ops-server.jar"]
