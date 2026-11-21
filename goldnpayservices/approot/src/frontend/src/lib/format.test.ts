import { describe, expect, it } from 'vitest';
import { formatINR, formatINRCompact, formatTenure } from './format';

describe('formatINR', () => {
  it('groups in the Indian system', () => {
    expect(formatINR(1_000_000)).toBe('₹10,00,000');
    expect(formatINR(999)).toBe('₹999');
    expect(formatINR(1234)).toBe('₹1,234');
    expect(formatINR(12345678.4)).toBe('₹1,23,45,678');
    expect(formatINR(0)).toBe('₹0');
    expect(formatINR(-1500)).toBe('-₹1,500');
  });
});

describe('formatINRCompact', () => {
  it('uses Cr, L and K', () => {
    expect(formatINRCompact(1_000_000)).toBe('₹10 L');
    expect(formatINRCompact(15_000_000)).toBe('₹1.5 Cr');
    expect(formatINRCompact(50_000)).toBe('₹50 K');
    expect(formatINRCompact(10_000_000)).toBe('₹1 Cr');
    expect(formatINRCompact(100_000)).toBe('₹1 L');
    expect(formatINRCompact(1000)).toBe('₹1 K');
    expect(formatINRCompact(999)).toBe('₹999');
    expect(formatINRCompact(1_234_567)).toBe('₹12.35 L');
    expect(formatINRCompact(-50_000)).toBe('-₹50 K');
  });
});

describe('formatTenure', () => {
  it('formats months and years', () => {
    expect(formatTenure(60)).toBe('5 years');
    expect(formatTenure(12)).toBe('1 year');
    expect(formatTenure(18)).toBe('1 year 6 months');
    expect(formatTenure(8)).toBe('8 months');
    expect(formatTenure(1)).toBe('1 month');
    expect(formatTenure(25)).toBe('2 years 1 month');
  });
});
