import { describe, expect, it } from 'vitest';
import { maxEligibleLoan, roundDownEligible } from './eligibility';

describe('roundDownEligible', () => {
  it('rounds down to the nearest 10,000', () => {
    expect(roundDownEligible(1_860_993.09)).toBe(1_860_000);
    expect(roundDownEligible(1_860_000)).toBe(1_860_000);
    expect(roundDownEligible(9_999.99)).toBe(0);
  });
  it('returns 0 for zero, negative or non-finite input', () => {
    expect(roundDownEligible(0)).toBe(0);
    expect(roundDownEligible(-5)).toBe(0);
    expect(roundDownEligible(Number.NaN)).toBe(0);
  });
  it('supports a custom step and rejects an invalid one', () => {
    expect(roundDownEligible(123_456, 1000)).toBe(123_000);
    expect(() => roundDownEligible(100, 0)).toThrow(RangeError);
  });
  it('matches the spec vector', () => {
    const { maxEmi, maxLoan } = maxEligibleLoan({
      monthlyIncome: 100_000,
      existingEmis: 10_000,
      annualRatePct: 10.5,
      months: 60,
    });
    expect(maxEmi).toBe(40_000);
    expect(roundDownEligible(maxLoan)).toBe(1_860_000);
  });
});

describe('maxEligibleLoan', () => {
  it('computes reference case 1', () => {
    const res = maxEligibleLoan({
      monthlyIncome: 100_000,
      existingEmis: 10_000,
      annualRatePct: 10.5,
      months: 60,
    });
    expect(res.maxEmi).toBe(40_000);
    expect(Math.abs(res.maxLoan - 1_860_993.09)).toBeLessThanOrEqual(0.01);
  });

  it('computes reference case 2', () => {
    const res = maxEligibleLoan({
      monthlyIncome: 50_000,
      existingEmis: 0,
      annualRatePct: 11,
      months: 36,
    });
    expect(res.maxEmi).toBe(25_000);
    expect(Math.abs(res.maxLoan - 763_621.86)).toBeLessThanOrEqual(0.01);
  });

  it('returns zeros when existing EMIs exceed capacity', () => {
    expect(
      maxEligibleLoan({
        monthlyIncome: 50_000,
        existingEmis: 30_000,
        annualRatePct: 10,
        months: 60,
      }),
    ).toEqual({ maxEmi: 0, maxLoan: 0 });
  });

  it('supports custom foir and zero rate', () => {
    const res = maxEligibleLoan({
      monthlyIncome: 100_000,
      existingEmis: 0,
      annualRatePct: 0,
      months: 10,
      foir: 0.4,
    });
    expect(res).toEqual({ maxEmi: 40_000, maxLoan: 400_000 });
  });

  it('throws RangeError on invalid input', () => {
    const base = { monthlyIncome: 1000, existingEmis: 0, annualRatePct: 10, months: 12 };
    expect(() => maxEligibleLoan({ ...base, months: 0 })).toThrow(RangeError);
    expect(() => maxEligibleLoan({ ...base, annualRatePct: -1 })).toThrow(RangeError);
    expect(() => maxEligibleLoan({ ...base, monthlyIncome: -1 })).toThrow(RangeError);
    expect(() => maxEligibleLoan({ ...base, existingEmis: -1 })).toThrow(RangeError);
    expect(() => maxEligibleLoan({ ...base, foir: 0 })).toThrow(RangeError);
  });
});
