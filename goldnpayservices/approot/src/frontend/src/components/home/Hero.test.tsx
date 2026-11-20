import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Hero from './Hero';

afterEach(cleanup);

function r() {
  render(
    <MemoryRouter>
      <Hero />
    </MemoryRouter>,
  );
}

describe('Hero', () => {
  it('shows the exact subtext', () => {
    r();
    expect(
      screen.getByText(
        'Compare offers from 20+ banks and NBFCs. Free, unbiased advice from application to disbursal.',
      ),
    ).toBeInTheDocument();
  });

  it('has the CTA links', () => {
    r();
    expect(screen.getByRole('link', { name: 'Get Free Consultation' })).toHaveAttribute('href', '/consultation');
    expect(screen.getByRole('link', { name: 'Check Eligibility' })).toHaveAttribute('href', '/eligibility');
  });

  it('includes the quick EMI card with a link to the detailed calculator', () => {
    r();
    expect(screen.getByRole('link', { name: 'Detailed calculator' })).toHaveAttribute('href', '/emi-calculator');
  });
});
