/// <reference types="vite/client" />
import { describe, expect, it } from 'vitest';
import { partners } from './partners';

const svgs = import.meta.glob('/public/images/partners/*.svg', {
  query: '?raw',
  import: 'default',
  eager: true,
}) as Record<string, string>;

const denylist =
  /HDFC|ICICI|\bSBI\b|State Bank|Axis|Kotak|Bajaj|IDFC|Yes Bank|PNB|Canara|Tata Capital|LIC Housing/i;

describe('partners data', () => {
  it('has 6 placeholder partners named Partner Bank 1..6', () => {
    expect(partners).toHaveLength(6);
    partners.forEach((p, i) => expect(p.name).toBe(`Partner Bank ${i + 1}`));
  });

  it('uses valid types', () => {
    partners.forEach((p) => expect(['Bank', 'NBFC', 'HFC']).toContain(p.type));
    expect(new Set(partners.map((p) => p.type)).size).toBe(3);
  });

  it('references .svg logos that exist in public/', () => {
    partners.forEach((p) => {
      expect(p.logo).toMatch(/^\/images\/partners\/partner-\d\.svg$/);
      const content = svgs[`/public${p.logo}`];
      expect(content, p.logo).toBeTruthy();
      expect(content).toContain('<svg');
    });
  });

  it('contains no real bank names', () => {
    partners.forEach((p) => expect(`${p.name} ${p.logo}`).not.toMatch(denylist));
  });
});
