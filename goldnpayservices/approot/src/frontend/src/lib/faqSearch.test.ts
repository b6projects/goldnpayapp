import { describe, expect, it } from 'vitest';
import { filterFaqs, groupFaqsByCategory } from './faqSearch';
import { faqs } from '../data/faqs';

describe('filterFaqs', () => {
  it('returns all items for empty or whitespace query', () => {
    expect(filterFaqs(faqs, '')).toHaveLength(faqs.length);
    expect(filterFaqs(faqs, '   ')).toHaveLength(faqs.length);
  });

  it('is case-insensitive and trims', () => {
    const r = filterFaqs(faqs, '  ARE YOU A LENDER ');
    expect(r.map((f) => f.q)).toEqual(['Are you a lender?']);
  });

  it('returns [] when nothing matches', () => {
    expect(filterFaqs(faqs, 'zzzzqqq')).toEqual([]);
  });

  it('matches partial words and does not treat the query as a regex', () => {
    expect(filterFaqs(faqs, 'lend').length).toBeGreaterThan(0);
    expect(filterFaqs(faqs, '.*')).toEqual([]);
  });

  it('searches question text only', () => {
    expect(filterFaqs(faqs, 'indicative')).toEqual([]);
  });
});

describe('groupFaqsByCategory', () => {
  it('orders General, Process, Fees, Eligibility', () => {
    expect(groupFaqsByCategory(faqs).map(([c]) => c)).toEqual(['General', 'Process', 'Fees', 'Eligibility']);
  });

  it('omits empty groups', () => {
    const only = faqs.filter((f) => f.category === 'Fees');
    expect(groupFaqsByCategory(only).map(([c]) => c)).toEqual(['Fees']);
    expect(groupFaqsByCategory([])).toEqual([]);
  });
});
