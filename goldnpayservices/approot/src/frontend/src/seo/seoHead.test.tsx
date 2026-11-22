import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { routes } from '../routes';

vi.mock('../components/tools/EmiChart', () => ({ default: () => null }));

// One render per file: react-helmet-async removes a page's head tags asynchronously, so a second
// render in the same document would see tags from the first.
describe('document head for a loan page', () => {
  it('sets title, description, canonical, Open Graph and structured data', async () => {
    render(<RouterProvider router={createMemoryRouter(routes, { initialEntries: ['/loans/gold-loan'] })} />);
    await screen.findByRole('heading', { level: 1, name: 'Gold Loan' });
    await waitFor(() =>
      expect(document.title).toBe('Gold Loan in Tamil Nadu – Compare Rates from 8.5% | GoldNPay'),
    );
    const meta = (selector: string) => document.querySelector(selector)?.getAttribute('content');
    expect(meta('meta[name="description"]')).toContain('gold loan');
    expect(document.querySelector('link[rel="canonical"]')?.getAttribute('href')).toMatch(/\/loans\/gold-loan$/);
    expect(meta('meta[property="og:title"]')).toBe(document.title);
    expect(meta('meta[property="og:image"]')).toMatch(/\/og-image\.png$/);
    expect(meta('meta[property="og:image:width"]')).toBe('1200');
    expect(meta('meta[property="og:image:height"]')).toBe('630');
    expect(meta('meta[name="twitter:card"]')).toBe('summary_large_image');
    const types = [...document.querySelectorAll('script[type="application/ld+json"]')].map(
      (el) => JSON.parse(el.textContent ?? '{}')['@type'],
    );
    expect(types).toEqual(['BreadcrumbList', 'LoanOrCredit', 'FAQPage']);
  });
});
