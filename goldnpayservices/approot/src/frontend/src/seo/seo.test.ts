import { describe, expect, it } from 'vitest';
import type { RouteObject } from 'react-router-dom';
import { routes } from '../routes';
import { loans } from '../data/loans';
import { faqs } from '../data/faqs';
import { site } from '../config/site';
import { absoluteUrl, indexablePaths, loanMeta, staticPageMeta } from './meta';
import { breadcrumbSchema, faqSchema, loanProductSchema, organizationSchema } from './schema';

function routePaths(list: RouteObject[]): string[] {
  return list.flatMap((r) => [...(r.path ? [r.path] : []), ...routePaths(r.children ?? [])]);
}

describe('seo meta', () => {
  it('lists every concrete route in the sitemap, plus one page per loan', () => {
    const concrete = routePaths(routes).filter((p) => p !== '*' && !p.includes(':'));
    const paths = indexablePaths();
    expect(paths.slice().sort()).toEqual([...concrete, ...loans.map((l) => `/loans/${l.slug}`)].sort());
    expect(paths.length).toBeGreaterThanOrEqual(15);
  });

  it('gives every page a unique title and a description of a sensible length', () => {
    const metas = [...Object.values(staticPageMeta), ...loans.map(loanMeta)];
    expect(new Set(metas.map((m) => m.title)).size).toBe(metas.length);
    expect(new Set(metas.map((m) => m.description)).size).toBe(metas.length);
    metas.forEach((m) => {
      expect(m.title.length, m.title).toBeLessThanOrEqual(70);
      expect(m.description.length, m.description).toBeGreaterThanOrEqual(100);
      expect(m.description.length, m.description).toBeLessThanOrEqual(165);
    });
  });

  it('builds loan titles as "<Loan> in <City> – Compare Rates from <min>% | GoldNPay"', () => {
    const gold = loans.find((l) => l.slug === 'gold-loan')!;
    expect(loanMeta(gold).title).toBe('Gold Loan in Tamil Nadu – Compare Rates from 8.5% | GoldNPay');
  });

  it('builds absolute URLs without trailing slashes (except the root)', () => {
    expect(absoluteUrl('/')).toMatch(/^https?:\/\/[^/]+\/$/);
    expect(absoluteUrl('/loans/')).toMatch(/^https?:\/\/[^/]+\/loans$/);
  });
});

describe('structured data', () => {
  it('describes the business on the homepage with matching name, address and phone', () => {
    const org = organizationSchema() as Record<string, any>; // eslint-disable-line @typescript-eslint/no-explicit-any
    expect(org['@type']).toBe('FinancialService');
    expect(org.name).toBe(site.name);
    expect(org.telephone).toBe(site.phoneE164);
    expect(site.address).toContain(org.address.postalCode);
    expect(site.address).toContain(org.address.addressLocality);
    expect(org.areaServed.name).toBe('Tamil Nadu');
    expect(org).not.toHaveProperty('sameAs'); // no social links configured
  });

  it('omits FAQ entries that are still owner placeholders', () => {
    const data = faqSchema(faqs) as { mainEntity: { name: string }[] };
    expect(JSON.stringify(data)).not.toContain('TODO(owner)');
    expect(data.mainEntity.length).toBe(faqs.filter((f) => !f.a.includes('TODO(owner)')).length);
    expect(faqSchema([{ q: 'q', a: 'TODO(owner): x' }])).toBeNull();
  });

  it('numbers breadcrumb items from 1 with absolute URLs', () => {
    const data = breadcrumbSchema([
      { name: 'Home', path: '/' },
      { name: 'Loans', path: '/loans' },
    ]) as { itemListElement: { position: number; item: string }[] };
    expect(data.itemListElement.map((i) => i.position)).toEqual([1, 2]);
    expect(data.itemListElement.every((i) => i.item.startsWith('http'))).toBe(true);
  });

  it('marks up each loan with its indicative rate, amount and term ranges', () => {
    loans.forEach((loan) => {
      const data = loanProductSchema(loan) as Record<string, any>; // eslint-disable-line @typescript-eslint/no-explicit-any
      expect(data['@type']).toBe('LoanOrCredit');
      expect(data.interestRate.minValue).toBe(loan.rateRange.min);
      expect(data.amount.maxValue).toBe(loan.amountRange.max);
      expect(data.loanTerm.unitCode).toBe('MON');
    });
  });
});
