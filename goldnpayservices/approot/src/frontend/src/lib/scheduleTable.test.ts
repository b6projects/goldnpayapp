import { describe, expect, it } from 'vitest';
import { buildScheduleTable } from './scheduleTable';

describe('buildScheduleTable', () => {
  const t = buildScheduleTable({ principal: 100000, ratePct: 12, months: 18 });

  it('has one monthly row per month and a final balance of about zero', () => {
    expect(t.monthlyRows).toHaveLength(18);
    expect(t.monthlyRows[0][0]).toBe(1);
    expect(Math.abs(t.monthlyRows[17][4])).toBeLessThanOrEqual(1);
  });

  it('groups months into years, the last one partial', () => {
    expect(t.yearlyRows.map((y) => y[0])).toEqual([1, 2]);
  });

  it('lists the loan details and a default or custom title', () => {
    expect(t.details[0]).toEqual(['Loan amount (INR)', 100000]);
    expect(t.details[2]).toEqual(['Tenure', '1 year 6 months']);
    expect(t.title).toBe('Loan repayment schedule');
    expect(
      buildScheduleTable({ principal: 1000, ratePct: 0, months: 2, loanTitle: 'Car Loan' }).title,
    ).toBe('Car Loan repayment schedule');
  });

  it('yearly principal adds up to the loan amount', () => {
    const sum = t.yearlyRows.reduce((a, y) => a + y[1], 0);
    expect(Math.abs(sum - 100000)).toBeLessThanOrEqual(2);
  });
});
