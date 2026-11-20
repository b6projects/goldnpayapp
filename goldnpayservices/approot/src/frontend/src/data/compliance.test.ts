import { describe, expect, it } from 'vitest';
import { site } from '../config/site';
import { faqs } from './faqs';
import { loans } from './loans';
import { partners } from './partners';
import { stats } from './stats';
import { testimonials } from './testimonials';

const corpus = JSON.stringify({ site, loans, partners, testimonials, faqs, stats });

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
  });

  it('does not name real banks or lenders', () => {
    expect(corpus).not.toMatch(realBanks);
  });

  it('describes rates as indicative', () => {
    expect(faqs.some((f) => /indicative/i.test(f.a))).toBe(true);
  });

  it('does not claim to be a lender', () => {
    expect(corpus).not.toMatch(/we are a (bank|lender)/i);
  });
});
