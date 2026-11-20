# GoldNPay Services — Functional Details

> Source of truth for the functional/business shape of the GoldNPay Services application (`goldnpayapp/goldnpayservices`). Written for consumption by JIRA-creation and other AI agents (see `.claude/agents/jira-item-creator.md`) that need to scope and describe feature/business work. See `TechnicalDetails.md` for the architecture/implementation view.
>
> **Status of this document (2026-11-20):** the repository contains no application code yet — only the folder skeleton under `approot/src/`. The business content below comes from the GoldnPay company profile and website work (services, journey, pricing basis, segments). Application features are therefore **intended scope, not implemented behaviour**.
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

## 4. User Journeys / Workflows

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
9. **Phase 1 MVP**: which modules in §3 are in the first release (suggested minimum: rate publishing, booking, visit management, evaluation/valuation, purchase + payout record, basic reports).
