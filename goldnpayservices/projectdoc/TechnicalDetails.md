# GoldNPay Services — Technical Details

> Source of truth for the technical shape of the codebase at `goldnpayapp/goldnpayservices`. Written for consumption by JIRA-creation and other AI agents that need to scope, size and describe technical work. See `FunctionalDetails.md` for the business view.
>
> **Status of this document (2026-11-21):** the **frontend** is built (sprint GPS_Sprint_1, GPS-2 to GPS-10, merged to `main`) and is documented as fact in §2A. `backend/`, `database/`, `docker/` and `scripts/` (which holds only empty `lbuildscripts/` and `lstartupscripts/` folders) are still empty: there is no backend code, database, Dockerfile or CI/CD pipeline. Sections 1 to 11 that concern those components stay **[UNVERIFIED]** proposals. The frontend is a static marketing site meant to serve both business lines; so far it implements only the loan-advisory line (see `FunctionalDetails.md` §3A), and the gold-buying line is not on the site yet.
>
> Markers: **[UNVERIFIED]** = not confirmed against source; **[FLAG]** = decision needed; **[INFERENCE]** = reasoned, not stated.

## 1. System Overview & Component Map

| Component | Path | Intended role | State |
|---|---|---|---|
| backend | `approot/src/backend/` | API and business logic: rates, bookings, visits, valuation, purchases, pledged release, payouts, inventory, users | Empty folder |
| frontend | `approot/src/frontend/` | Public marketing website for both business lines (loan advisory built, gold buying pending); see §2A | **Built** (v0.1.0) |
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
| frontend | Vite + React 18 + TypeScript + Tailwind CSS 3 | **Decided and built**; details in §2A. |
| database | TBD | MySQL if aligned with GoldBucks **[INFERENCE]**; `config.database.engine` is `TODO`. |

Once chosen, record versions and key dependencies here, and fill in `project.yaml` → `testing.modules.*` (build, unit_test, package, artifacts, lint, audit).

## 2A. Frontend (implemented) — `approot/src/frontend`

**Stack** (`package.json`, name `goldnpay-frontend`, v0.1.0, Node >= 18.18): React 18.3, TypeScript 5.6 (strict), Vite 5, React Router 7 (`createBrowserRouter`), Tailwind CSS 3.4 (+ PostCSS/autoprefixer), Recharts 3 (EMI pie chart), lucide-react (icons). Test and quality tooling: Vitest 2 with jsdom, Testing Library (react, dom, user-event, jest-dom), `@vitest/coverage-v8`, ESLint 9 (typescript-eslint, react-hooks, react-refresh, prettier config; zero warnings allowed) and Prettier.

**Scripts:** `dev`, `build` (`tsc --noEmit && vite build`, output `dist/`), `preview`, `lint`, `format`, `test` (`vitest run`), `test:watch`, `test:coverage`, `typecheck`. Install with `npm ci`.

**Architecture:** a static single-page app with no backend calls, no persisted state and no environment variables.
- `src/main.tsx` and `src/App.tsx` mount `RouterProvider`; `src/routes.tsx` defines all routes under one `Layout` (header, footer, WhatsApp button, mobile sticky bar, skip link, `Suspense` outlet). `Home` loads eagerly; every other page is `React.lazy` code-split.
- `src/pages/` holds the 13 route pages; `src/components/` is grouped into `layout`, `home`, `loans`, `tools` (EmiCalculator, EmiChart, AmortizationTable, EligibilityChecker, SliderField), `forms`, `legal` and `ui` primitives (Accordion, Badge, Breadcrumb, Button, Card, Checkbox, Input, Select, Slider, Tabs and others). `components/seo/` is empty.
- `src/data/` holds typed seed content (loans, faqs, home, partners, stats, testimonials); `src/types/index.ts` defines `Loan`, `Faq`, `Partner`, `Testimonial`, `Stat` and the `LOAN_SLUGS` union.
- `src/config/site.ts` is the single source for business details and two flags: `showSampleTestimonials` and `legalReviewed`. Every value the owner must supply is tagged `TODO(owner)` (find with `grep -rn "TODO(owner)" src`; list in `OWNER_INPUTS.md`).
- `src/lib/` holds pure, unit-tested logic: `emi.ts`, `eligibility.ts` (default FOIR 0.5, validation via `RangeError`), `format.ts` (INR and tenure formatting), `whatsapp.ts` (`wa.me` link builder), `consultation.ts` (`/consultation?loan=&amount=` builder), `faqSearch.ts`, and `analytics.ts` (no-op `trackEvent`). `src/hooks/` holds `useDebouncedValue` and `useScrollToTop`.
- Styling: colour tokens live only in `tailwind.config.ts` (primary `#0F4C81`, accent `#F2A900`, success `#25D366`, plus text, muted, background, surface); components use token classes, not raw hex. Fonts: Inter (body) and Poppins (headings).
- Hosting: `public/_redirects` contains `/* /index.html 200` (SPA fallback, Netlify/Cloudflare Pages style), plus `robots.txt` and static partner SVGs. The hosting target itself is not decided **[FLAG]**.

**Testing:** Vitest config in `vite.config.ts` (jsdom, setup `src/test/setup.ts`, CSS disabled). Coverage is measured on `src/lib/**` with a **90% threshold** on statements, lines, functions and branches. Last run (2026-11-21): **49 test files, 280 tests, all passing**. Tests sit beside the code as `*.test.ts(x)`, covering lib, data integrity (including a compliance test and an owner-inputs test), UI primitives, pages and routes.

**Known gaps:**
- `/consultation` is a `PlaceholderPage`, and `components/forms/ConsultationForm.tsx` is a placeholder with its final prop contract (`defaultLoan`, `lockLoan`, `defaultAmount`, `compact`). Both are replaced by the real form in GPS-9 (M8).
- `analytics.ts` does nothing; no SEO/meta tags per page; no E2E tests; no CI/CD; no Dockerfile.
- `README.md` is minimal; the M10 README is meant to fold in the owner-input list.
- `.claude/config/project.yaml` → `testing.modules.frontend` should be filled with the commands above (install `npm ci`, build `npm run build`, test `npm test`, lint `npm run lint`, artifact `dist/index.html`) **[verify what is currently set]**. Frontend `permissions.allow` entries likewise.

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

The frontend has 280 unit/component tests (see §2A) with a 90% coverage gate on `src/lib/**`. The backend has none (no code). Still open **[INFERENCE]**: scaffold unit tests when the backend starts (the `jira-item-tester` agent reports a module with no `unit_test` command as `NO TESTS CONFIGURED`), decide on E2E tooling (`config.testing.e2e.spec_glob`), and decide whether to enable coverage in the agent config (`config.testing.coverage.enabled`).

There is no CI/CD pipeline **[gap — worth a JIRA item]**.

## 11. Open Questions / Flags for Verification

1. Backend stack and versions (§2). The frontend stack is settled (§2A). Then fill in `project.yaml` → `testing.modules.*`.
2. Database engine, and the schema migration tool (Flyway / Liquibase / raw SQL scripts).
3. API style and versioning convention (§4).
4. Resolved in part: a new Vite + React website was built in `frontend/` (§2A) instead of porting the earlier CRA site. Open: does the back-office app also live in `frontend/`, or in a separate module? How will the consultation form submit (WhatsApp hand-off only, or a backend lead API)?
5. Field-executive app: responsive web vs. native/PWA.
6. Integration providers for the rate feed, payouts, WhatsApp, SMS/email and maps (§7).
7. Environments (DEV/UAT/PRD, plus QA?) and the hosting target.
8. CI/CD pipeline and where it lives.
9. What is in `goldnpayapp/temp/` and whether it holds requirements or designs that belong in these docs.
