import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, fireEvent, render, screen, within } from '@testing-library/react';
import AmortizationTable from './AmortizationTable';
import { buildSchedule } from '../../lib/emi';
import { formatINR } from '../../lib/format';

afterEach(cleanup);

const toggle = () => screen.getByRole('button', { name: 'View repayment schedule' });
const yearButtons = () => screen.queryAllByRole('button', { name: /^Year \d+$/ });

describe('AmortizationTable', () => {
  it('is collapsed by default', () => {
    render(<AmortizationTable principal={500000} ratePct={11} months={36} />);
    expect(toggle()).toHaveAttribute('aria-expanded', 'false');
    expect(screen.queryByRole('table')).toBeNull();
  });

  it('expands and collapses with aria-expanded', () => {
    render(<AmortizationTable principal={500000} ratePct={11} months={36} />);
    fireEvent.click(toggle());
    expect(toggle()).toHaveAttribute('aria-expanded', 'true');
    expect(screen.getByRole('table', { name: /yearly repayment schedule/i })).toBeInTheDocument();
    fireEvent.click(toggle());
    expect(toggle()).toHaveAttribute('aria-expanded', 'false');
    expect(screen.queryByRole('table')).toBeNull();
  });

  it('shows ceil(n/12) yearly rows including a partial last year', () => {
    render(<AmortizationTable principal={500000} ratePct={11} months={30} />);
    fireEvent.click(toggle());
    expect(yearButtons()).toHaveLength(3);
  });

  it('expands a year to show its months and ends with a zero balance', () => {
    render(<AmortizationTable principal={500000} ratePct={11} months={30} />);
    fireEvent.click(toggle());
    const years = yearButtons();
    expect(years[2]).toHaveAttribute('aria-expanded', 'false');
    fireEvent.click(years[2]);
    expect(years[2]).toHaveAttribute('aria-expanded', 'true');
    const monthly = screen.getByRole('table', { name: 'Monthly breakdown for year 3' });
    const rows = within(monthly).getAllByRole('row');
    expect(rows).toHaveLength(1 + 6);
    const last = within(rows[rows.length - 1]).getAllByRole('cell');
    expect(last[0]).toHaveTextContent('30');
    expect(last[4]).toHaveTextContent(formatINR(0));
    const schedule = buildSchedule(500000, 11, 30);
    expect(last[1]).toHaveTextContent(formatINR(schedule[29].emi));
    fireEvent.click(years[2]);
    expect(screen.queryByRole('table', { name: /monthly breakdown/i })).toBeNull();
  });
});

describe('AmortizationTable downloads', () => {
  it('offers PDF and Excel downloads without opening the schedule', () => {
    render(<AmortizationTable principal={500000} ratePct={11} months={36} />);
    expect(screen.getByRole('button', { name: 'Download PDF' })).toBeEnabled();
    expect(screen.getByRole('button', { name: 'Download Excel' })).toBeEnabled();
    expect(screen.queryByRole('table')).toBeNull();
  });
});
