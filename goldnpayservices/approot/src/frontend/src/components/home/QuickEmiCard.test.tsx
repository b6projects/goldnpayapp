import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import QuickEmiCard from './QuickEmiCard';
import { calculateEmi } from '../../lib/emi';
import { formatINR } from '../../lib/format';

afterEach(cleanup);

function r() {
  render(
    <MemoryRouter>
      <QuickEmiCard />
    </MemoryRouter>,
  );
}

describe('QuickEmiCard', () => {
  it('shows the default EMI', () => {
    r();
    expect(screen.getByTestId('quick-emi-value')).toHaveTextContent(formatINR(calculateEmi(500000, 11, 36)));
  });

  it('updates the EMI when a slider changes', () => {
    r();
    fireEvent.change(screen.getByLabelText(/Loan amount/), { target: { value: '1000000' } });
    expect(screen.getByTestId('quick-emi-value')).toHaveTextContent(formatINR(calculateEmi(1000000, 11, 36)));
    fireEvent.change(screen.getByLabelText(/Tenure/), { target: { value: '60' } });
    expect(screen.getByTestId('quick-emi-value')).toHaveTextContent(formatINR(calculateEmi(1000000, 11, 60)));
  });

  it('labels the 11% rate as indicative', () => {
    r();
    expect(screen.getByText('Rate: 11% p.a. (indicative)')).toBeInTheDocument();
    expect(screen.getByText(/Indicative estimate/)).toBeInTheDocument();
  });
});
