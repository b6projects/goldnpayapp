/** Loan eligibility maths. Pure; unrounded results. */

export interface EligibilityInput {
  monthlyIncome: number;
  existingEmis: number;
  annualRatePct: number;
  months: number;
  /** Fixed obligation to income ratio; default 0.5. */
  foir?: number;
}

export interface EligibilityResult {
  maxEmi: number;
  maxLoan: number;
}

export function maxEligibleLoan(input: EligibilityInput): EligibilityResult {
  const { monthlyIncome, existingEmis, annualRatePct, months, foir = 0.5 } = input;
  if (!Number.isFinite(monthlyIncome) || monthlyIncome < 0) {
    throw new RangeError('monthlyIncome must be a non-negative finite number');
  }
  if (!Number.isFinite(existingEmis) || existingEmis < 0) {
    throw new RangeError('existingEmis must be a non-negative finite number');
  }
  if (!Number.isFinite(annualRatePct) || annualRatePct < 0) {
    throw new RangeError('annualRatePct must be a non-negative finite number');
  }
  if (!Number.isInteger(months) || months <= 0) {
    throw new RangeError('months must be a positive integer');
  }
  if (!Number.isFinite(foir) || foir <= 0 || foir > 1) {
    throw new RangeError('foir must be in (0, 1]');
  }
  const maxEmi = Math.max(0, monthlyIncome * foir - existingEmis);
  if (maxEmi === 0) return { maxEmi: 0, maxLoan: 0 };
  const r = annualRatePct / 12 / 100;
  if (r === 0) return { maxEmi, maxLoan: maxEmi * months };
  const growth = Math.pow(1 + r, months);
  return { maxEmi, maxLoan: (maxEmi * (growth - 1)) / (r * growth) };
}
