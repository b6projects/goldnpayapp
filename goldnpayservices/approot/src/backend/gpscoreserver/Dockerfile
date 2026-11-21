# ═══════════════════════════════════════════════════════
#  GoldNPay Core Server image (com.gps.cs)
#  Stage 1 builds the jar with the Maven wrapper; the unit and
#  integration tests run during the build (a red test fails the image).
#  Stage 2 is a small JRE runtime, non-root, health-checked.
#
#  Build:  docker build -t goldnpay-core-server:1.0.0 .
#          (normally via scripts/lbuildscripts/build-backend-core.sh)
# ═══════════════════════════════════════════════════════
ARG JAVA_VERSION=25

# ---------- Stage 1: build ----------
FROM eclipse-temurin:${JAVA_VERSION}-jdk-alpine AS build
WORKDIR /build

# Resolve dependencies first so this layer is cached until the pom changes.
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src src
RUN ./mvnw -B -ntp package

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

ARG APP_VERSION=dev
ARG GIT_COMMIT=unknown
LABEL org.opencontainers.image.title="goldnpay-core-server" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"

# Fonts are not needed by Core today (it draws no images). Add fontconfig and ttf-dejavu here if that changes.

RUN addgroup -S -g 10001 gnp && adduser -S -u 10001 -G gnp gnp \
    && mkdir -p /app/server/serverlogs && chown -R gnp:gnp /app
WORKDIR /app
COPY --from=build --chown=gnp:gnp /build/target/core-server.jar /app/core-server.jar

USER gnp
ENV SERVER_PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=UTC"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD wget -qO- "http://127.0.0.1:${SERVER_PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

# ENV (DEV/UAT/PRD) selects the Spring profile: dev, uat or prd.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dspring.profiles.active=$(echo \"${ENV:-dev}\" | tr 'A-Z' 'a-z') -jar /app/core-server.jar"]
