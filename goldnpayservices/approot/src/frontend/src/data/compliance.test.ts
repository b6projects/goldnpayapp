/// <reference types="vite/client" />
import { describe, expect, it } from 'vitest';
import { site } from '../config/site';
import { faqs } from './faqs';
import { loans } from './loans';
import { partners } from './partners';
import { stats } from './stats';
import { testimonials } from './testimonials';

const corpus = JSON.stringify({ site, loans, partners, testimonials, faqs, stats });

// Raw source of the content pages added in GPS-10 (M9).
const pageSources = Object.values(
  import.meta.glob(
    [
      '/src/pages/About.tsx',
      '/src/pages/Contact.tsx',
      '/src/pages/Faq.tsx',
      '/src/pages/Partners.tsx',
      '/src/pages/Privacy.tsx',
      '/src/pages/Terms.tsx',
      '/src/pages/Disclaimer.tsx',
      '/src/components/legal/LegalPage.tsx',
    ],
    { query: '?raw', import: 'default', eager: true },
  ) as Record<string, string>,
).join('\n');

const prohibited = [
  /guaranteed approval/i,
  /lowest rate guaranteed/i,
  /guaranteed lowest/i,
  /100% approval/i,
  /assured approval/i,
  /instant approval/i,
];

const realBanks =
  /HDFC|ICICI|\bSBI\b|State Bank|Axis Bank|Kotak|Bajaj|IDFC|Yes Bank|Punjab National|\bPNB\b|Bank of Baroda|Canara|Tata Capital|LIC Housing|Piramal|Muthoot|Manappuram|IndusInd|Federal Bank/i;

describe('compliance (spec 11)', () => {
  it.each(prohibited.map((r) => [r.source, r] as const))('has no prohibited claim %s', (_n, re) => {
    expect(corpus).not.toMatch(re);
    expect(pageSources).not.toMatch(re);
  });

  it('does not name real banks or lenders', () => {
    expect(corpus).not.toMatch(realBanks);
    expect(pageSources).not.toMatch(realBanks);
  });

  it('scans the page sources (guards against an empty glob)', () => {
    expect(pageSources.length).toBeGreaterThan(1000);
  });

  it('describes rates as indicative', () => {
    expect(faqs.some((f) => /indicative/i.test(f.a))).toBe(true);
  });

  it('does not claim to be a lender', () => {
    expect(corpus).not.toMatch(/we are a (bank|lender)/i);
    expect(pageSources).not.toMatch(/we are a (bank|lender)/i);
  });
});
