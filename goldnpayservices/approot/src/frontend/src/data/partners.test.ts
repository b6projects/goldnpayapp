import { describe, expect, it } from 'vitest';
import { partners } from './partners';

describe('partners data', () => {
  it('lists the Indian public and private sector banks', () => {
    expect(partners.filter((p) => p.sector === 'Public')).toHaveLength(12);
    expect(partners.filter((p) => p.sector === 'Private')).toHaveLength(21);
  });

  it('has unique names and a short monogram for each bank', () => {
    expect(new Set(partners.map((p) => p.name)).size).toBe(partners.length);
    partners.forEach((p) => {
      expect(p.name.length).toBeGreaterThan(2);
      expect(p.short.length).toBeGreaterThan(0);
      expect(p.short.length).toBeLessThanOrEqual(5);
    });
  });

  it('uses valid types', () => {
    partners.forEach((p) => expect(['Bank', 'NBFC', 'HFC']).toContain(p.type));
  });

  it('uses text badges only (no logo images)', () => {
    partners.forEach((p) => expect(p).not.toHaveProperty('logo'));
  });
});
