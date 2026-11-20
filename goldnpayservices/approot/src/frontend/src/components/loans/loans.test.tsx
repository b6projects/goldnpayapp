import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import LoanHeader from './LoanHeader';
import RelatedLoans from './RelatedLoans';
import { getRelatedLoans } from './getRelatedLoans';
import Breadcrumb from '../ui/Breadcrumb';
import { loans } from '../../data/loans';
import { formatINRCompact, formatTenure } from '../../lib/format';

afterEach(cleanup);

describe('LoanHeader', () => {
  it.each(loans)('renders title, badges and indicative note for $slug', (loan) => {
    render(<LoanHeader loan={loan} />);
    expect(screen.getByRole('heading', { level: 1, name: loan.title })).toBeInTheDocument();
    const badges = within(screen.getByRole('list'));
    expect(badges.getByText(`Rates from ${loan.rateRange.min}% p.a.`)).toBeInTheDocument();
    expect(badges.getByText(`Up to ${formatINRCompact(loan.amountRange.max)}`)).toBeInTheDocument();
    expect(badges.getByText(`Tenure up to ${formatTenure(loan.tenureMonths.max)}`)).toBeInTheDocument();
    expect(screen.getByText(/indicative and subject to/)).toBeInTheDocument();
  });
});

describe('RelatedLoans', () => {
  it.each(loans)('returns 3 other loans deterministically for $slug', (loan) => {
    const related = getRelatedLoans(loan);
    expect(related).toHaveLength(3);
    expect(related.map((l) => l.slug)).not.toContain(loan.slug);
    expect(getRelatedLoans(loan)).toEqual(related);
  });

  it('renders 3 links', () => {
    render(
      <MemoryRouter>
        <RelatedLoans loan={loans[loans.length - 1]} />
      </MemoryRouter>,
    );
    expect(screen.getAllByRole('listitem')).toHaveLength(3);
    expect(screen.getAllByRole('link', { name: /Learn more about/ })).toHaveLength(3);
  });
});

describe('Breadcrumb', () => {
  it('marks the last item as the current page', () => {
    render(
      <MemoryRouter>
        <Breadcrumb items={[{ label: 'Home', to: '/' }, { label: 'Loans', to: '/loans' }, { label: 'X' }]} />
      </MemoryRouter>,
    );
    expect(screen.getByRole('navigation', { name: 'Breadcrumb' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Loans' })).toHaveAttribute('href', '/loans');
    expect(screen.getByText('X')).toHaveAttribute('aria-current', 'page');
  });
});
