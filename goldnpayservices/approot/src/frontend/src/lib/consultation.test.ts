import { describe, expect, it } from 'vitest';
import { buildConsultationPath } from './consultation';

describe('buildConsultationPath', () => {
  it('includes loan and amount', () => {
    expect(buildConsultationPath({ loan: 'home-loan', amount: 1860000 })).toBe(
      '/consultation?loan=home-loan&amount=1860000',
    );
  });
  it('omits the loan when absent', () => {
    expect(buildConsultationPath({ amount: 500000 })).toBe('/consultation?amount=500000');
  });
  it('omits the amount when absent or not positive', () => {
    expect(buildConsultationPath({ loan: 'personal-loan' })).toBe(
      '/consultation?loan=personal-loan',
    );
    expect(buildConsultationPath({ loan: 'personal-loan', amount: 0 })).toBe(
      '/consultation?loan=personal-loan',
    );
    expect(buildConsultationPath({})).toBe('/consultation');
  });
  it('truncates the amount to an integer', () => {
    expect(buildConsultationPath({ amount: 1234.9 })).toBe('/consultation?amount=1234');
  });
  it('encodes the slug', () => {
    expect(buildConsultationPath({ loan: 'a b&c' })).toBe('/consultation?loan=a+b%26c');
  });
});
