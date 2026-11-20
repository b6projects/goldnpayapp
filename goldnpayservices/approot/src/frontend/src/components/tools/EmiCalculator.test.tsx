import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import EmiCalculator from './EmiCalculator';
import { loans } from '../../data/loans';
import { calculateEmi } from '../../lib/emi';
import { formatINR } from '../../lib/format';

afterEach(cleanup);

describe('EmiCalculator (placeholder)', () => {
  it.each(loans)('starts at the defaults and limits sliders to the ranges for $slug', (loan) => {
    render(<EmiCalculator loan={loan} />);
    const expected = formatINR(
      calculateEmi(loan.defaults.amount, loan.defaults.rate, loan.defaults.tenureMonths),
    );
    expect(screen.getByTestId('emi-value')).toHaveTextContent(expected);
    const [amount, tenure] = screen.getAllByRole('slider');
    expect(amount).toHaveAttribute('min', String(loan.amountRange.min));
    expect(amount).toHaveAttribute('max', String(loan.amountRange.max));
    expect(tenure).toHaveAttribute('min', String(loan.tenureMonths.min));
    expect(tenure).toHaveAttribute('max', String(loan.tenureMonths.max));
    expect(screen.getAllByText(/indicative/i).length).toBeGreaterThan(0);
  });
});
