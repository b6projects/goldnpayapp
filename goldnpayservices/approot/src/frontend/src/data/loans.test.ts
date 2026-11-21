import { describe, expect, it } from 'vitest';
import { LOAN_SLUGS } from '../types';
import { getLoanBySlug, loans } from './loans';

// [rate range, amount range, tenure range, defaults as amount/rate/tenure]
const spec: Record<string, [number[], number[], number[], number[]]> = {
  'personal-loan': [[10.5, 24], [50000, 4000000], [12, 60], [500000, 11, 36]],
  'home-loan': [[8.35, 11], [500000, 50000000], [60, 360], [2500000, 8.5, 240]],
  'business-loan': [[14, 26], [100000, 5000000], [12, 60], [1000000, 16, 36]],
  'loan-against-property': [[9, 14], [500000, 50000000], [60, 180], [3000000, 10, 120]],
  'gold-loan': [[8.5, 18], [10000, 5000000], [3, 36], [200000, 9.5, 12]],
  'car-loan': [[8.7, 14], [100000, 10000000], [12, 84], [800000, 9, 60]],
  'balance-transfer': [[8.35, 16], [100000, 50000000], [12, 360], [2000000, 9, 180]],
};

describe('loans data', () => {
  it('has 7 loans, unique slugs, in spec order', () => {
    expect(loans).toHaveLength(7);
    const slugs = loans.map((l) => l.slug);
    expect(new Set(slugs).size).toBe(7);
    expect(slugs).toEqual([...LOAN_SLUGS]);
  });

  it.each(loans.map((l) => [l.slug, l] as const))('%s matches the spec table', (slug, loan) => {
    const [rate, amount, tenure, def] = spec[slug];
    expect(loan.rateRange).toEqual({ min: rate[0], max: rate[1] });
    expect(loan.amountRange).toEqual({ min: amount[0], max: amount[1] });
    expect(loan.tenureMonths).toEqual({ min: tenure[0], max: tenure[1] });
    expect(loan.defaults).toEqual({ amount: def[0], rate: def[1], tenureMonths: def[2] });
  });

  it.each(loans.map((l) => [l.slug, l] as const))('%s is internally consistent', (_slug, l) => {
    expect(l.rateRange.min).toBeLessThan(l.rateRange.max);
    expect(l.amountRange.min).toBeLessThan(l.amountRange.max);
    expect(l.tenureMonths.min).toBeLessThan(l.tenureMonths.max);
    expect(l.defaults.amount).toBeGreaterThanOrEqual(l.amountRange.min);
    expect(l.defaults.amount).toBeLessThanOrEqual(l.amountRange.max);
    expect(l.defaults.rate).toBeGreaterThanOrEqual(l.rateRange.min);
    expect(l.defaults.rate).toBeLessThanOrEqual(l.rateRange.max);
    expect(l.defaults.tenureMonths).toBeGreaterThanOrEqual(l.tenureMonths.min);
    expect(l.defaults.tenureMonths).toBeLessThanOrEqual(l.tenureMonths.max);
  });

  it.each(loans.map((l) => [l.slug, l] as const))('%s respects content limits', (_slug, l) => {
    expect(l.title).toBeTruthy();
    expect(l.icon).toBeTruthy();
    expect(l.shortDescription.length).toBeGreaterThan(0);
    expect(l.shortDescription.length).toBeLessThanOrEqual(120);
    expect(l.intro.length).toBeGreaterThan(0);
    expect(l.idealFor.length).toBeGreaterThan(0);
    expect(l.eligibility.length).toBeGreaterThanOrEqual(3);
    expect(l.eligibility.length).toBeLessThanOrEqual(5);
    expect(l.features.length).toBeGreaterThanOrEqual(4);
    expect(l.features.length).toBeLessThanOrEqual(6);
    expect(l.faqs.length).toBeGreaterThanOrEqual(3);
    expect(l.faqs.length).toBeLessThanOrEqual(4);
    expect(l.documents.salaried.length).toBeGreaterThan(0);
    expect(l.documents.selfEmployed.length).toBeGreaterThan(0);
    expect(l.seo.title).toBeTruthy();
    expect(l.seo.description).toBeTruthy();
  });

  it('getLoanBySlug returns known loans and undefined for unknown', () => {
    expect(getLoanBySlug('home-loan')?.slug).toBe('home-loan');
    expect(getLoanBySlug('nope')).toBeUndefined();
    expect(getLoanBySlug('')).toBeUndefined();
  });
});
