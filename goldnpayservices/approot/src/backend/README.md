# GoldNPay backend

Two Spring Boot 3.5 / Java 25 services, built from `temp/GoldNPay-Backend-Spec.md`.

| Server | Package | Port (DEV/UAT/PRD) | Does |
|---|---|---|---|
| `gpscoreserver` | `com.gps.cs` | 8220 / 8221 / 8222 | Users, auth (JWT), KYC, bank accounts, gold rate, settings, media, reports. **Owns the schema** (Flyway `V1__baseline.sql`). |
| `gpsopsserver` | `com.gps.os` | 8230 / 8231 / 8232 | Email (Thymeleaf templates), Telegram bots, Discord, Twitter/X, image overlay, schedulers. Validates the schema; never alters it. |

## Build and test

```bash
cd gpscoreserver && ./mvnw clean verify     # 31 tests
cd gpsopsserver  && ./mvnw clean verify     # 62 tests
```

Tests use H2 in MySQL mode with the real Flyway script (Ops loads Core's script, so schema drift fails Ops' tests).
`MySqlMigrationTest` / `MySqlSchemaTest` run the same checks on a real MySQL 8.4 with Testcontainers and are **skipped when
Docker is not available**. Run them where Docker exists before the first deploy.

Docker images and compose: see [`../docker/README.md`](../docker/README.md).

## Configuration

Every secret is `${ENV_VAR}` with no fallback. A missing one stops startup with a message that names it (`RequiredConfigCheck`).
Spring would otherwise leave the literal `${VAR}` in place and start. The Spring profile comes from `ENV` (`DEV`/`UAT`/`PRD`).
`BOOTSTRAP_ADMIN_EMAIL` / `BOOTSTRAP_ADMIN_PASSWORD` (optional, first start only) create the first ADMIN.

## Where this departs from the spec

| Spec | What was built | Why |
|---|---|---|
| `telegrambots-spring-boot-starter` 6.5.0 | Direct HTTPS calls to the Telegram Bot API (`TelegramApiClient`) | The starter registers itself through `spring.factories`, which Spring Boot 3 ignores. |
| Services as interface + impl | Concrete `@Service` classes | One implementation each; no second one planned. |
| Compose `depends_on: service_started` | Ops waits for Core `service_healthy` | Ops only validates the schema; it must wait until Core's migration has run. |
| Ops `CacheRefreshScheduler` under ShedLock | Not locked | The cache lives in each instance's memory, so every instance must refresh. |
| Bucket4j rate limiting | In-memory fixed window (20/min/IP on `/tc/v1/auth/**`) | Same behaviour, no extra dependency. Per instance. |
| — | `SocialPostRequest.overlayRate` | Lets Core ask Ops to draw the live rate onto the image when a new rate goes live. |
| — | `GET /tc/v1/public/images/{id}` | Without it the image URLs returned by the public list endpoints would lead nowhere. |
| — | Activation reminders at least 2 days apart (`app.reminders.min-gap-days`) | A daily job would otherwise send all 3 reminders on 3 consecutive days. |
| — | Email, Telegram and social sends are idempotent on `requestId` (`:<channel>` for social) | Core retries timed-out calls with the same request, so a retry must not send twice. |

## Known limits

- **Twitter/X:** `twitter4j` 4.0.7 uses the v1.1 API. X restricts v1.1 posting on most tiers, so posts may come back `FAILED`.
  This is reported per channel and never blocks the others. Verify the credentials and tier before relying on it.
- **Email queue is in memory.** A restart while mails are queued leaves their `notification_logs` rows `QUEUED`.
- **Email branding** (logo URL, address, contact, unsubscribe URL) is placeholder text in Ops' `application.yml` under
  `app.mail.brand`. Replace it with the real values.
- Rate limiting and the Telegram pacing queue are per instance.

## Before go-live
Twitter/X: twitter4j 4.0.7 uses the v1.1 API, which X restricts on most tiers, so posts may come back FAILED. That is reported per channel and doesn't block the others. Check your credentials and tier.
Email branding: the logo URL, address and unsubscribe link in Ops' application.yml are placeholders.
UAT/PRD API host: API_HOST in .env.uat and .env.prd is empty with a TODO, because I don't know Core's public URL.
Secrets file: the variable list is in docker/README.md.
