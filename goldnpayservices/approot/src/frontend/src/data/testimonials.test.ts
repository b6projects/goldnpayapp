import { describe, expect, it } from 'vitest';
import { testimonials } from './testimonials';

describe('testimonials data', () => {
  it('has 3 samples', () => {
    expect(testimonials).toHaveLength(3);
  });

  it('has complete entries with rating 1-5', () => {
    testimonials.forEach((t) => {
      expect(t.name).toBeTruthy();
      expect(t.city).toBeTruthy();
      expect(t.loanType).toBeTruthy();
      expect(t.quote).toBeTruthy();
      expect([1, 2, 3, 4, 5]).toContain(t.rating);
    });
  });
});
