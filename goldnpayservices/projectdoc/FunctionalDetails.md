# GoldNPay Services — Functional Details

> Source of truth for the functional/business shape of the GoldNPay Services application (`goldnpayapp/goldnpayservices`). Written for consumption by JIRA-creation and other AI agents (see `.claude/agents/jira-item-creator.md`) that need to scope and describe feature/business work. See `TechnicalDetails.md` for the architecture/implementation view.
>
> **Status of this document (2026-11-21):** the only application code is the **public marketing website** in `approot/src/frontend/` (sprint GPS_Sprint_1, GPS-2 to GPS-10, merged to `main`). It is a **loan-advisory** site (see §3A), not the gold-buying operations platform. `backend/`, `database/` and `docker/` are still empty, so everything in §1–§9 about the gold-buying operations platform remains **intended scope, not implemented behaviour**. The business content in those sections comes from the GoldnPay company profile.
>
> **[CONFIRMED-BIZ] Two business lines, one site:** GoldNPay offers both (a) gold buying and pledged-gold release (§1–§9) and (b) loan advisory through partner banks and NBFCs (§3A). The website serves both. **Gap:** the site built so far covers only the loan-advisory line (its only gold content is the "Gold Loan" page). Gold-buying content (live rate, gold calculator, doorstep booking, pledged release) is not on the site yet (see §9, question 9).
>
> Markers used (the agents treat these as mandatory talking points, never as settled fact):
> - **[CONFIRMED-BIZ]** — stated in the GoldnPay company profile / website content.
> - **[INFERENCE]** — a reasonable reading of the business model, not yet confirmed for the app.
> - **[FLAG]** — a decision or fact that must be confirmed with the product owner before it is built.
> - **[UNVERIFIED]** — mentioned somewhere but not checked against a current source.
>
> Update this document as features are built: replace intent with file-path evidence, as the GoldBucks equivalent does.

## 1. Product Purpose

GoldnPay is a gold-buying business that gives individuals a safe, transparent and fast way to convert gold into cash, through physical branches and a doorstep service network **[CONFIRMED-BIZ]**. Mission: let every gold owner unlock the true value of their gold with no hidden charges and instant payment **[CONFIRMED-BIZ]**.

GoldNPay Services is the software platform behind that business. Its intended job is to run the operation end to end — enquiries and bookings, doorstep visits, evaluation and valuation, pledged-gold release cases, customer payouts, and the back office **[INFERENCE]**. Whether it also includes the public website and a customer-facing app is open **[FLAG — see §9]**.

Brand promise: *"Your gold. Its true value. In your hands — today."* — highest rate, most transparent process, fastest payment **[CONFIRMED-BIZ]**.

## 2. User Roles & Personas

### Customers (business segments) **[CONFIRMED-BIZ]**
| Segment | Need |
|---|---|
| Homemakers & families | Sell inherited/unused jewellery; value trust, transparency and doorstep convenience. |
| Pledged-gold borrowers | Gold loans at banks/NBFCs; need release assistance and cash-out of the remaining value. |
| Business owners | Liquidate gold quickly for working capital; fast payment, large-value handling. |
| First-time / digital-native sellers | Compare rates online first; need a low-pressure, trusted process. |

### System roles **[FLAG — none defined yet; proposed starting set for confirmation]**
| Role (proposed) | Description |
|---|---|
| `CUSTOMER` | Books visits, sees quotes and transaction history (only if a customer-facing app is in scope). |
| `FIELD_EXECUTIVE` | Doorstep evaluator: receives assigned visits, records items, weight, purity and valuation, triggers payment. |
| `BRANCH_STAFF` | Walk-in evaluation and payout at a branch. |
| `OPS_MANAGER` | Assigns visits, approves high-value quotes and pledged-release cases, manages the published gold rate. |
| `FINANCE` | Payout approval/reconciliation, settlement of bank/NBFC loans for pledged release. |
| `ADMIN` | Users, roles, branches, service pin codes, settings. |

## 3. Core Feature Modules

All modules are **intended scope [INFERENCE]**, derived from the confirmed services and customer journey. Nothing is implemented yet.

| Module | Description | Grounding |
|---|---|---|
| Live gold rate | Publish the buying rate, based on IBJA/MJDTA market rates; shown on the website and at branches. | [CONFIRMED-BIZ] live rate visible on website and at branch |
| Rate calculator | Customer-facing estimate from weight and purity. | [CONFIRMED-BIZ] website gold calculator; Phase 2 rate-alert tool |
| Enquiry & booking | Capture walk-in, call and WhatsApp enquiries; book doorstep visits by pin code and time slot. | [CONFIRMED-BIZ] "call/WhatsApp us to book a doorstep visit" |
| Doorstep visit management | Assign executives, schedule, track visit status, capture the outcome. | [CONFIRMED-BIZ] doorstep service across 50+ pin codes (Phase 1) |
| Evaluation & valuation | Record each item (type, gross/net weight, purity), apply the live rate, show a full price breakdown with no hidden deductions. | [CONFIRMED-BIZ] transparent valuation; certified evaluators, done in front of the customer |
| Cash for gold (purchase) | Customer accepts the quote, then the purchase transaction and receipt are created. | [CONFIRMED-BIZ] jewellery, coins, bars, broken pieces |
| Pledged gold release | Case workflow: capture the loan (bank/NBFC, loan amount, pledged items), settle the lender, collect the gold, evaluate, pay the customer the remaining value, and charge a service fee. | [CONFIRMED-BIZ] release service + service fee |
| Payments / payouts | Pay within 30 minutes by cash, NEFT, RTGS, IMPS or UPI; record and reconcile payouts. | [CONFIRMED-BIZ] payment channels and 30-minute target |
| Customer KYC | Capture ID/address proof for each seller. | [FLAG] required documents and regulatory basis to be confirmed |
| Inventory & resale | Track purchased gold until it is sold to refineries/bullion dealers; record the resale price to compute the spread. | [INFERENCE] from the revenue model (spread on resale) |
| Branch management | Branches, staff, service pin codes. | [CONFIRMED-BIZ] multi-branch model (Phase 2: 5–10 branches) |
| Reporting | Daily purchases, volume (grams), spread per gram, payouts, pledged cases, executive performance. | [INFERENCE] |
| Notifications | Booking confirmations, visit reminders, payment receipts (SMS/WhatsApp/email). | [INFERENCE] channel to be confirmed |
| Admin / settings | Users and roles, rate source, margins, fees, pin codes. | [INFERENCE] |

## 3A. Implemented: Public Loan-Advisory Website (`approot/src/frontend`)

Status: **built and merged** (GPS-2 to GPS-10). A static, front-end-only site: no backend, no database, no login, nothing stored. Currency is INR. Rates, amounts and tenures are indicative and shown with disclaimers; final terms are set by the lender.

### Pages (routes in `src/routes.tsx`)
| Route | Page | What it does | Status |
|---|---|---|---|
| `/` | Home | Nine sections: hero with quick EMI card, stats, loan grid, how it works, why us, partners, testimonials, FAQ preview, CTA band | Done (GPS-6) |
| `/loans` | Loans list | Cards for the seven loan products | Done (GPS-7) |
| `/loans/:slug` | Loan detail | Tabbed content (intro, ideal for, eligibility, documents for salaried/self-employed, features, FAQs), embedded EMI calculator, consultation form, related loans, breadcrumb | Done (GPS-7) |
| `/emi-calculator` | EMI calculator | Amount, rate and tenure sliders; monthly EMI, total interest, total payable; pie chart; year-wise amortization table | Done (GPS-8) |
| `/eligibility` | Eligibility checker | Estimates max EMI and max loan from monthly income and existing EMIs | Done (GPS-8) |
| `/consultation` | Free consultation | **Placeholder page only**; the real form is a later item (GPS-9 / M8) | **Pending** |
| `/about` | About | Company story and "how we're paid" | Done (GPS-10); copy needs owner input |
| `/faq` | FAQ | Category grouping (General, Process, Fees, Eligibility) and live question search | Done (GPS-10) |
| `/contact` | Contact | Phone, WhatsApp, email, address, hours and map (map hidden when no embed URL) | Done (GPS-10) |
| `/partners` | Partners | Partner bank/NBFC/HFC list | Done (GPS-10); sample data |
| `/privacy`, `/terms`, `/disclaimer` | Legal pages | Template text with a "template" banner until legal review is recorded | Done (GPS-10); **not legally reviewed** |
| `*` | Not found | 404 page | Done |

### Loan products
Personal, Home, Business, Loan against property, Gold, Car, and Balance transfer (`src/data/loans.ts`). Each carries an indicative rate range, amount range, tenure range, eligibility criteria, document lists (salaried vs self-employed), features, FAQs and EMI-calculator defaults.

### Calculators and rules
- **EMI**: standard reducing-balance formula; supports a 0% rate; schedule and year-wise summary (`src/lib/emi.ts`).
- **Eligibility**: max EMI = monthly income × FOIR − existing EMIs, floored at 0. **FOIR defaults to 50%.** Max loan is the present value of that EMI over the tenure; the displayed figure is rounded **down to the nearest ₹10,000** (`src/lib/eligibility.ts`). The result is an estimate, not an offer.
- **Consultation hand-off**: calculators and loan pages link to `/consultation?loan=<slug>&amount=<int>` to prefill the form (`src/lib/consultation.ts`).
- **WhatsApp**: the consultation message is built as a `wa.me` link from the form details; nothing is logged or stored (`src/lib/whatsapp.ts`).
- **FAQ search**: case-insensitive substring match on the question text.

### Site-wide elements
Header with mobile navigation, footer, floating WhatsApp button, mobile sticky call/WhatsApp bar, skip-to-content link, scroll-to-top on navigation, and a no-op analytics hook (`trackEvent`) ready for a provider.

### Content that is placeholder and needs the owner (`approot/src/frontend/OWNER_INPUTS.md`)
Business name, domain, phone/WhatsApp/email, address, map URL, cities served, grievance officer, social links and founding year (all in `src/config/site.ts`, marked `TODO(owner)`); the fee policy ("how we are paid", the "no-cost consultation" claim and the fee FAQ); real partner names and logo permission; four verifiable stats; real, consented testimonials (sample testimonials are shown with a "Sample" badge while `showSampleTestimonials` is true); and legal review of Privacy, Terms and Disclaimer (`legalReviewed` flag).

### Not yet built
Real consultation form with explicit consent checkbox (the compliance item stays pending until it ships), lead capture or CRM hand-off, analytics provider, SEO/meta management (`components/seo` is empty), and everything in §3 for the gold-buying platform.

## 4. User Journeys / Workflows

### 4.0 Loan consultation (website, implemented except the form) 
1. The visitor browses loans, or uses the EMI calculator or eligibility checker.
2. They choose "Get a free consultation" (loan type and amount prefilled via the query string).
3. They land on `/consultation`. Today this is a placeholder; the intended flow is to submit details and open a prefilled WhatsApp message to the business.
4. A consultant follows up. Lender selection and final terms happen offline.


### 4.1 Cash for gold (branch or doorstep) **[CONFIRMED-BIZ]**
1. **Contact** — the customer walks into a branch or calls/WhatsApps to book a doorstep visit; the live rate is visible on the website and at the branch.
2. **Expert evaluation** — trained, certified evaluators assess purity using established industry methods, openly in front of the customer.
3. **Transparent valuation** — the price is calculated live from IBJA/MJDTA rates, weight and purity, with a full breakdown and no hidden deductions.
4. **Instant payment** — paid within 30 minutes by cash, NEFT, RTGS, IMPS or UPI.

### 4.2 Pledged gold release **[CONFIRMED-BIZ, detail INFERENCE]**
1. The customer brings the loan details (lender, loan account, outstanding amount).
2. GoldnPay settles the outstanding loan with the bank/NBFC.
3. The released gold is collected and evaluated (as 4.1 steps 2–3).
4. The customer is paid the evaluated value minus the settled loan amount and the service fee.
5. **[FLAG]** Who carries the risk between settlement and evaluation (e.g. a valuation below the loan amount), and what agreement the customer signs first, must be defined before this is built.

### 4.3 Doorstep visit **[INFERENCE]**
Booking → assignment to a field executive → visit → evaluation → quote accepted/declined → payment → receipt → gold custody hand-over to the branch.

## 5. Business Rules

| Rule | Value | Source |
|---|---|---|
| Pricing basis | Live IBJA/MJDTA market rates × weight × purity | [CONFIRMED-BIZ] |
| Payment SLA | Within 30 minutes of quote acceptance | [CONFIRMED-BIZ] |
| Payment channels | Cash, NEFT, RTGS, IMPS, UPI | [CONFIRMED-BIZ] |
| Items accepted | Gold jewellery, coins, bars, broken pieces | [CONFIRMED-BIZ] |
| Out of scope | Silver, diamond, platinum (removed from scope) | [CONFIRMED-BIZ] |
| Purity testing | "Certified evaluation by expert evaluators" — XRF testing was removed from the stated scope | [CONFIRMED-BIZ]; [FLAG] the evaluation method to record in the app is undefined |
| Margin | ₹30–80/gram spread on resale to refineries/bullion dealers | [CONFIRMED-BIZ] (business plan figure — internal, never customer-facing) |
| Pledged release fee | A service fee applies | [CONFIRMED-BIZ]; [FLAG] amount/formula undefined |
| Transparency | Full valuation breakdown shown; no hidden deductions | [CONFIRMED-BIZ] |
| Doorstep coverage | 50+ pin codes in Phase 1 | [CONFIRMED-BIZ] |

**[FLAG] Compliance rules not yet captured**, all of which shape the data model and must be confirmed: KYC requirements for gold purchase, cash payment limits (e.g. Income Tax Act cash-transaction limits), record retention, GST treatment on purchase/resale, PAN capture thresholds, and AML reporting obligations. None of these should be assumed by an agent; raise them when a related item is scoped.

## 6. Localization / i18n

The target market is India, with emphasis on South India **[CONFIRMED-BIZ]**. The language set (e.g. English + Tamil/Hindi, as GoldBucks used for brochures) is not decided **[FLAG]**. Currency is INR; weights are in grams **[INFERENCE]**.

## 7. Integrations

| Integration | Purpose | Status |
|---|---|---|
| IBJA / MJDTA gold rate | Source of the live buying rate | [CONFIRMED-BIZ] as the pricing basis; [FLAG] feed/API and refresh interval undecided |
| Payments (UPI / NEFT / RTGS / IMPS) | Customer payouts | [CONFIRMED-BIZ] channels; [FLAG] banking/payout provider undecided |
| WhatsApp | Booking and enquiries | [CONFIRMED-BIZ] channel; [FLAG] WhatsApp Business API vs. manual |
| SMS / email | Confirmations and receipts | [INFERENCE] |
| Maps / geolocation | Doorstep routing and pin-code coverage | [INFERENCE] |
| Banks / NBFCs | Pledged-loan settlement | [CONFIRMED-BIZ] process; manual vs. integrated [FLAG] |
| Refinery / bullion buyers | Resale of purchased gold | [INFERENCE] likely manual records |

## 8. Admin & Operations Capabilities **[INFERENCE]**

- Publish and pause the buying rate; set margins per purity band.
- Manage branches, staff, roles and service pin codes.
- Visit dispatch board: unassigned, assigned, in-progress and completed visits.
- Approval queues: high-value purchases, pledged-release cases, payouts.
- Custody and inventory: gold received per branch/executive, transfers, resale lots.
- Reconciliation: payouts vs. purchases, resale proceeds vs. purchase cost (spread).
- Reports: volume, value, spread, cases, executive performance.

## 9. Open Questions / Flags for Verification

1. **Product scope**: is GoldNPay Services the internal operations platform only, or does it also include the public website (built earlier as a React app) and a customer app? Does the earlier React website move into `approot/src/frontend`?
2. **Roles**: confirm the role set in §2.
3. **Evaluation method**: what is recorded for purity (karat declaration, touchstone, acid test, a device reading) now that XRF is out of scope?
4. **Pledged release**: risk handling, customer agreement, fee formula, and whether lenders are integrated or handled manually.
5. **Compliance**: KYC, cash limits, PAN, GST, record retention, AML (§5).
6. **Rate feed**: the IBJA/MJDTA source, refresh interval, and who can override the published rate.
7. **Payout provider** for UPI/NEFT/RTGS/IMPS, and whether cash is recorded only or integrated with a cash-management process.
8. **Languages** for the customer-facing surfaces.
9. **Site structure for two business lines**: how should the site present both (shared navigation, a landing split, or a separate gold-buying section)? Which gold-buying content comes first (live rate, gold calculator, doorstep booking enquiry, pledged release)? Which tagline applies (it is currently "Find the right loan, with expert help.")? Does the consultation form need a service selector (loan vs. sell gold)?
10. **Fee policy** for the loan service (how the business is paid; basis for the "no-cost consultation" claim), and the consent and privacy wording for the consultation form.
11. **Phase 1 MVP**: which modules in §3 are in the first release (suggested minimum: rate publishing, booking, visit management, evaluation/valuation, purchase + payout record, basic reports).
