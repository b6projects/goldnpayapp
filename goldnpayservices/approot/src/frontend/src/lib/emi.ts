/** EMI maths. Pure functions; full precision internally, callers round for display. */

export interface ScheduleRow {
  month: number;
  emi: number;
  interest: number;
  principal: number;
  balance: number;
}

export interface EmiSummary {
  emi: number;
  totalInterest: number;
  totalPayment: number;
}

function validate(principal: number, annualRatePct: number, months: number): void {
  if (!Number.isFinite(principal) || principal <= 0) {
    throw new RangeError('principal must be a positive finite number');
  }
  if (!Number.isFinite(annualRatePct) || annualRatePct < 0) {
    throw new RangeError('annualRatePct must be a non-negative finite number');
  }
  if (!Number.isInteger(months) || months <= 0) {
    throw new RangeError('months must be a positive integer');
  }
}

/** Monthly EMI (unrounded). Rate is an annual percentage, e.g. 10.5. */
export function calculateEmi(principal: number, annualRatePct: number, months: number): number {
  validate(principal, annualRatePct, months);
  const r = annualRatePct / 12 / 100;
  if (r === 0) return principal / months;
  const growth = Math.pow(1 + r, months);
  return (principal * r * growth) / (growth - 1);
}

/** Month-by-month amortisation schedule; the last row's balance is exactly 0. */
export function buildSchedule(
  principal: number,
  annualRatePct: number,
  months: number,
): ScheduleRow[] {
  const emi = calculateEmi(principal, annualRatePct, months);
  const r = annualRatePct / 12 / 100;
  const rows: ScheduleRow[] = [];
  let balance = principal;
  for (let month = 1; month <= months; month++) {
    const interest = balance * r;
    const isLast = month === months;
    const principalPart = isLast ? balance : emi - interest;
    balance = isLast ? 0 : balance - principalPart;
    rows.push({ month, emi, interest, principal: principalPart, balance });
  }
  return rows;
}

/** EMI, total payment and total interest (all unrounded). */
export function summarize(principal: number, annualRatePct: number, months: number): EmiSummary {
  const emi = calculateEmi(principal, annualRatePct, months);
  const totalPayment = emi * months;
  return { emi, totalPayment, totalInterest: totalPayment - principal };
}
