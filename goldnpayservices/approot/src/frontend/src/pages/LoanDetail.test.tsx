import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import LoanDetail from './LoanDetail';
import NotFound from './NotFound';
import { loans } from '../data/loans';
import { formatINRCompact, formatTenure } from '../lib/format';
import { getRelatedLoans } from '../components/loans/getRelatedLoans';

afterEach(cleanup);

function renderAt(path: string) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/loans/:slug" element={<LoanDetail />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('LoanDetail', () => {
  it.each(loans)('renders all sections for $slug', (loan) => {
    renderAt(`/loans/${loan.slug}`);
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    expect(screen.getByRole('heading', { level: 1, name: loan.title })).toBeInTheDocument();

    const crumbs = screen.getByRole('navigation', { name: 'Breadcrumb' });
    expect(within(crumbs).getByRole('link', { name: 'Home' })).toHaveAttribute('href', '/');
    expect(within(crumbs).getByRole('link', { name: 'Loans' })).toHaveAttribute('href', '/loans');
    expect(within(crumbs).getByText(loan.title)).toHaveAttribute('aria-current', 'page');

    expect(screen.getByText(loan.intro)).toBeInTheDocument();
    const header = within(document.querySelector('header') as HTMLElement);
    expect(header.getByText(`Rates from ${loan.rateRange.min}% p.a.`)).toBeInTheDocument();
    expect(header.getByText(`Up to ${formatINRCompact(loan.amountRange.max)}`)).toBeInTheDocument();
    expect(header.getByText(`Tenure up to ${formatTenure(loan.tenureMonths.max)}`)).toBeInTheDocument();
    expect(header.getAllByText(/indicative/).length).toBeGreaterThan(0);

    for (const name of [
      'Ideal for',
      'Key features',
      'Eligibility',
      'Documents required',
      'EMI calculator',
      'Frequently asked questions',
      'Related loans',
    ]) {
      expect(screen.getByRole('heading', { level: 2, name })).toBeInTheDocument();
    }
    expect(screen.getByRole('tab', { name: 'Salaried' })).toHaveAttribute('aria-selected', 'true');
    expect(document.querySelectorAll('details')).toHaveLength(loan.faqs.length);

    const related = getRelatedLoans(loan);
    const relatedSection = screen
      .getByRole('heading', { level: 2, name: 'Related loans' })
      .closest('section') as HTMLElement;
    expect(within(relatedSection).getAllByRole('listitem')).toHaveLength(3);
    related.forEach((l) => {
      expect(within(relatedSection).getByRole('heading', { name: l.title })).toBeInTheDocument();
    });
    expect(within(relatedSection).queryByRole('heading', { name: loan.title })).toBeNull();
  });

  it.each(loans)('locks the consultation form to $slug and places it last', (loan) => {
    renderAt(`/loans/${loan.slug}`);
    expect(screen.getByTestId('consultation-locked-loan')).toHaveTextContent(loan.title);
    const column = screen.getByTestId('consultation-column');
    expect(column.className).toContain('lg:sticky');
    const related = screen.getByRole('heading', { level: 2, name: 'Related loans' });
    const faqs = screen.getByRole('heading', { level: 2, name: 'Frequently asked questions' });
    expect(related.compareDocumentPosition(column) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(faqs.compareDocumentPosition(column) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  });

  it('renders the 404 page for an unknown slug', () => {
    renderAt('/loans/does-not-exist');
    expect(screen.getByRole('heading', { level: 1, name: 'Page Not Found' })).toBeInTheDocument();
    expect(screen.queryByTestId('consultation-locked-loan')).toBeNull();
  });
});
