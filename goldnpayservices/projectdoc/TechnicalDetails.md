# GoldNPay Services — Technical Details

> Source of truth for the technical shape of the codebase at `goldnpayapp/goldnpayservices`. Written for consumption by JIRA-creation and other AI agents that need to scope, size and describe technical work. See `FunctionalDetails.md` for the business view.
>
> **Status of this document (2026-11-20):** greenfield. The repository holds the Claude SDLC configuration (`.claude/`) and an empty application skeleton (`approot/src/{backend,database,docker,frontend,scripts}`, with no files in any of them). There is **no source code, build file, test, Dockerfile or pipeline yet**. Every technology choice below is **[UNVERIFIED]** until the code exists. When a component is added, replace the TBD rows with file-path evidence, as the GoldBucks equivalent does.
>
> Markers: **[UNVERIFIED]** = not confirmed against source; **[FLAG]** = decision needed; **[INFERENCE]** = reasoned, not stated.

## 1. System Overview & Component Map

| Component | Path | Intended role | State |
|---|---|---|---|
| backend | `approot/src/backend/` | API and business logic: rates, bookings, visits, valuation, purchases, pledged release, payouts, inventory, users | Empty folder |
| frontend | `approot/src/frontend/` | Back-office / operations web app (and possibly the public site) | Empty folder |
| database | `approot/src/database/` | Schema, migrations, seed/config SQL, backup scripts | Empty folder |
| docker | `approot/src/docker/` | Dockerfiles and Compose stacks per environment | Empty folder |
| scripts | `approot/src/scripts/` | Ops/dev helper scripts | Empty folder |
| SDLC config | `.claude/` | Claude agents, commands, templates and `config/project.yaml` | Present |

Repository layout: `goldnpayapp/` is the git root (it holds `.git`), containing `goldnpayservices/` and a sibling `temp/` folder **[UNVERIFIED — contents of `temp/` not reviewed]**.

**[INFERENCE]** A mobile-friendly field-executive app (for doorstep visits) is likely needed. Whether that is a responsive web app inside `frontend/` or a separate component is open **[FLAG]**.

## 2. Component Tech Stacks

Not chosen yet **[FLAG]**. `.claude/config/project.yaml` → `testing.modules.{backend,frontend}.stack` is still `TODO`.

| Component | Stack | Notes |
|---|---|---|
| backend | TBD | If the GoldBucks pattern is reused: Java + Spring Boot, Maven wrapper, JWT security, MySQL **[INFERENCE]**. The agent config already ships a `spring-boot` stack profile. |
| frontend | TBD | An earlier GoldnPay website exists as a Create React App project (CSS Modules, custom hooks). For a new build, Vite + React matches the GoldBucks client **[INFERENCE]**. The agent config ships a `react` stack profile. |
| database | TBD | MySQL if aligned with GoldBucks **[INFERENCE]**; `config.database.engine` is `TODO`. |

Once chosen, record versions and key dependencies here, and fill in `project.yaml` → `testing.modules.*` (build, unit_test, package, artifacts, lint, audit).

## 3. Backend Architecture

Not built. Proposed layering, consistent with the `spring-boot` stack profile, for agreement before the first JIRA items **[INFERENCE]**:

- **Layering:** Controller → DTO → Service → Repository → Entity; thin controllers; validation on request DTOs; transactions on multi-step money/inventory writes.
- **Proposed modules:** `auth` (users, roles, JWT), `rates` (rate feed, published rate, history), `booking` (enquiries, slots, pin codes), `visit` (assignment, status, field capture), `valuation` (items, weights, purity, quote), `purchase` (transactions, receipts), `pledge` (release cases, lender settlement, fees), `payout` (channels, status, reconciliation), `inventory` (custody, transfers, resale lots), `branch`, `kyc`, `notification`, `reports`, `admin/settings`, `scheduler` (rate refresh, reminders).
- **Cross-cutting needs:** audit trail on every price, valuation and payout change; idempotent payout creation; encryption at rest for KYC documents and bank details; role-based access per §2 of the functional doc.

## 4. REST API Surface

None exists. **[FLAG]** Decide the API style before the first backend item: resource-style REST, or the RPC-style `POST` convention GoldBucks uses. Record the base path/versioning convention here once agreed.

## 5. Data Model

None exists. Candidate core entities **[INFERENCE]** (prefixes are only a suggestion, following GoldBucks' per-module table-prefix convention):

| Group | Candidate entities |
|---|---|
| Identity | `usr_user`, `usr_role`, `usr_user_role`, `usr_branch_staff` |
| Customer | `cst_customer`, `cst_kyc_document`, `cst_bank_account` |
| Rates | `rt_rate_source`, `rt_published_rate`, `rt_rate_history` |
| Booking & visits | `bk_enquiry`, `bk_booking`, `vs_visit`, `vs_visit_event`, `br_branch`, `br_service_pincode` |
| Valuation & purchase | `vl_valuation`, `vl_item` (type, gross/net weight, purity, rate applied, value), `pr_purchase`, `pr_receipt` |
| Pledged release | `pl_case`, `pl_lender`, `pl_settlement`, `pl_fee` |
| Payout | `py_payout` (channel, amount, reference, status), `py_reconciliation` |
| Inventory | `iv_lot`, `iv_custody_transfer`, `iv_resale` |
| Platform | `app_setting`, `app_scheduler`, `app_scheduler_history`, `aud_audit_log`, `ntf_notification` |

Money should be stored as fixed-point decimals (never floating point) and weights with defined precision (e.g. grams to 3 decimals) **[FLAG — confirm precision]**.

## 6. Security & Compliance (technical)

**[INFERENCE]** These follow from the business and need decisions before the related items are built:
- KYC files and bank details are PII: encrypted storage, access logging, a retention policy.
- Payout and pricing actions need maker/checker approval above a threshold, plus a full audit trail.
- Secrets live in environment variables or a secret store, never in `application.yml` or `.env` files committed to git. GoldBucks committed SMTP credentials in `application.yml`; avoid repeating that.
- Rate overrides must be role-restricted and logged.

## 7. Integrations (technical)

All undecided **[FLAG]**: the IBJA/MJDTA rate feed (API vs. scraping vs. manual entry), payout provider/bank API, WhatsApp Business API, SMS/email provider, maps/geocoding. Record the client class, config keys and env vars here as each one is added.

## 8. Infrastructure & Deployment

`approot/src/docker/` exists but is empty. The SDLC config expects Dockerfiles at `approot/src/docker/backend.Dockerfile` and `approot/src/docker/frontend.Dockerfile` (`project.yaml` → `testing.modules.*.dockerfile`) **[UNVERIFIED — placeholder paths]**. Environments are assumed to be DEV / UAT / PRD (matching `config.database.environments`) **[FLAG — add QA if needed, as GoldBucks has]**. Hosting target undecided.

## 9. Build Tooling

None yet. When added, record here and in `project.yaml`:
- backend: build / test / package commands and the artifact path.
- frontend: install / build / test / lint / audit commands and the `dist/` artifact check.
- The `.claude/settings.json` `permissions.allow` entries for those commands.

## 10. Testing & Quality Gaps

No tests exist (there is no code yet). Recommendation for the first sprint **[INFERENCE]**: set up unit-test scaffolding in both modules from day one (the `jira-item-tester` agent reports a module with no `unit_test` command as `NO TESTS CONFIGURED`), decide on E2E tooling (`config.testing.e2e.spec_glob`), and decide whether to enable coverage (`config.testing.coverage.enabled`).

There is no CI/CD pipeline **[gap — worth a JIRA item]**.

## 11. Open Questions / Flags for Verification

1. Backend and frontend stacks and versions (§2), then fill in `project.yaml` → `testing.modules.*`.
2. Database engine, and the schema migration tool (Flyway / Liquibase / raw SQL scripts).
3. API style and versioning convention (§4).
4. Whether the earlier GoldnPay React website is brought into this repo, and where.
5. Field-executive app: responsive web vs. native/PWA.
6. Integration providers for the rate feed, payouts, WhatsApp, SMS/email and maps (§7).
7. Environments (DEV/UAT/PRD, plus QA?) and the hosting target.
8. CI/CD pipeline and where it lives.
9. What is in `goldnpayapp/temp/` and whether it holds requirements or designs that belong in these docs.
