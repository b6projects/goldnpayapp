import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import LoanGrid from './LoanGrid';
import { loans } from '../../data/loans';

afterEach(cleanup);

function r() {
  render(
    <MemoryRouter>
      <LoanGrid />
    </MemoryRouter>,
  );
}

describe('LoanGrid', () => {
  it('renders 7 cards with details, indicative rate, icon and link', () => {
    r();
    const items = screen.getAllByRole('listitem');
    expect(items).toHaveLength(7);
    loans.forEach((loan, i) => {
      const item = items[i];
      expect(within(item).getByRole('heading', { level: 3, name: loan.title })).toBeInTheDocument();
      expect(within(item).getByText(loan.shortDescription)).toBeInTheDocument();
      expect(item).toHaveTextContent(`From ${loan.rateRange.min}% p.a.`);
      expect(item).toHaveTextContent('indicative');
      expect(item.querySelector('svg')).not.toBeNull();
      expect(within(item).getByRole('link', { name: `Learn more about ${loan.title}` })).toHaveAttribute(
        'href',
        `/loans/${loan.slug}`,
      );
    });
  });

  it('uses the responsive column classes', () => {
    r();
    const list = screen.getByRole('list');
    expect(list.className).toContain('grid-cols-1');
    expect(list.className).toContain('sm:grid-cols-2');
    expect(list.className).toContain('lg:grid-cols-4');
  });

  it('shows the indicative-rate disclaimer', () => {
    r();
    expect(screen.getByText(/Rates shown are indicative/)).toBeInTheDocument();
  });
});
