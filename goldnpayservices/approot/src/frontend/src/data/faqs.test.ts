import { describe, expect, it } from 'vitest';
import { faqs } from './faqs';

const find = (q: string) => faqs.find((f) => f.q === q);

describe('faqs data', () => {
  it('has at least 10 FAQs with valid categories', () => {
    expect(faqs.length).toBeGreaterThanOrEqual(10);
    faqs.forEach((f) => {
      expect(f.q).toBeTruthy();
      expect(f.a).toBeTruthy();
      expect(['General', 'Process', 'Fees', 'Eligibility']).toContain(f.category);
    });
  });

  it('covers all four categories and has unique questions', () => {
    expect(new Set(faqs.map((f) => f.category)).size).toBe(4);
    expect(new Set(faqs.map((f) => f.q)).size).toBe(faqs.length);
  });

  it('has the lender-fees FAQ and no unfinished answers', () => {
    expect(find('Does the lender charge any fees?')).toBeDefined();
    faqs.forEach((f) => expect(f.a).not.toContain('TODO(owner)'));
  });

  it('has the lender FAQ saying consultant/DSA', () => {
    const a = find('Are you a lender?')?.a ?? '';
    expect(a).toMatch(/^No/);
    expect(a).toMatch(/consultant|DSA/);
  });

  it('has the credit score FAQ', () => {
    expect(find('Will checking eligibility affect my credit score?')?.a).toContain(
      'No, this tool does not check your credit score',
    );
  });

  it('has the lenders FAQ linking to /partners', () => {
    expect(find('Which lenders do you work with?')?.a).toContain('/partners');
  });
});
