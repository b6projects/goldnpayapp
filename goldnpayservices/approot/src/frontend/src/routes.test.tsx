import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { routes } from './routes';
import { site } from './config/site';

function renderAt(path: string) {
  const router = createMemoryRouter(routes, { initialEntries: [path] });
  render(<RouterProvider router={router} />);
}

const cases: [string, string][] = [
  ['/', site.tagline],
  ['/loans', 'Loan Types'],
  ['/loans/personal-loan', 'Personal Loan'],
  ['/emi-calculator', 'EMI Calculator'],
  ['/eligibility', 'Loan Eligibility Calculator'],
  ['/consultation', 'Get Free Consultation'],
  ['/about', 'About Us'],
  ['/faq', 'Frequently Asked Questions'],
  ['/contact', 'Contact Us'],
  ['/partners', 'Partner Disclosure'],
  ['/privacy', 'Privacy Policy'],
  ['/terms', 'Terms of Use'],
  ['/disclaimer', 'Disclaimer'],
];

describe('routes', () => {
  it.each(cases)('renders %s', async (path, heading) => {
    renderAt(path);
    expect(
      await screen.findByRole('heading', { level: 1, name: heading }, { timeout: 5000 }),
    ).toBeInTheDocument();
  });

  it('renders NotFound for an unknown loan slug', async () => {
    renderAt('/loans/nope');
    expect(
      await screen.findByRole('heading', { level: 1, name: 'Page Not Found' }),
    ).toBeInTheDocument();
  });

  it('renders NotFound for unknown paths', async () => {
    renderAt('/does-not-exist');
    expect(
      await screen.findByRole('heading', { level: 1, name: 'Page Not Found' }),
    ).toBeInTheDocument();
  });
});
