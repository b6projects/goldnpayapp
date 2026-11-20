import { describe, expect, it } from 'vitest';
import { buildSchedule, calculateEmi, summarize } from './emi';

const cases = [
  { p: 1_000_000, r: 10, n: 12, emi: 87915.89, interest: 54990.65 },
  { p: 500_000, r: 12, n: 60, emi: 11122.22, interest: 167333.43 },
  { p: 2_500_000, r: 8.5, n: 240, emi: 21695.58, interest: 2706939.4 },
  { p: 120_000, r: 0, n: 12, emi: 10000, interest: 0 },
];

describe('emi', () => {
  it.each(cases)('reference case $p/$r/$n', ({ p, r, n, emi, interest }) => {
    expect(Math.abs(calculateEmi(p, r, n) - emi)).toBeLessThanOrEqual(0.01);
    const s = summarize(p, r, n);
    expect(Math.abs(s.emi - emi)).toBeLessThanOrEqual(0.01);
    expect(Math.abs(s.totalInterest - interest)).toBeLessThanOrEqual(0.5);
    expect(s.totalPayment).toBeCloseTo(p + s.totalInterest, 6);
  });

  it('throws RangeError on invalid input', () => {
    expect(() => calculateEmi(0, 10, 12)).toThrow(RangeError);
    expect(() => calculateEmi(-1, 10, 12)).toThrow(RangeError);
    expect(() => calculateEmi(1000, -1, 12)).toThrow(RangeError);
    expect(() => calculateEmi(1000, 10, 0)).toThrow(RangeError);
    expect(() => calculateEmi(1000, 10, 12.5)).toThrow(RangeError);
    expect(() => calculateEmi(Number.NaN, 10, 12)).toThrow(RangeError);
    expect(() => buildSchedule(1000, 10, 0)).toThrow(RangeError);
  });

  it.each(cases)('schedule is consistent for $p/$r/$n', ({ p, r, n }) => {
    const rows = buildSchedule(p, r, n);
    expect(rows).toHaveLength(n);
    expect(rows[rows.length - 1].balance).toBe(0);
    expect(rows.reduce((a, x) => a + x.principal, 0)).toBeCloseTo(p, 4);
    expect(rows.reduce((a, x) => a + x.interest, 0)).toBeCloseTo(
      summarize(p, r, n).totalInterest,
      2,
    );
    for (let i = 1; i < rows.length; i++) {
      expect(rows[i].balance).toBeLessThanOrEqual(rows[i - 1].balance);
    }
  });

  it('0% schedule has no interest', () => {
    const rows = buildSchedule(1200, 0, 12);
    expect(rows.every((x) => x.interest === 0)).toBe(true);
    expect(rows[0].principal).toBe(100);
  });
});
