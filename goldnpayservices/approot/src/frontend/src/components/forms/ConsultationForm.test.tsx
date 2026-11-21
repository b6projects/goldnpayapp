import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ConsultationForm from './ConsultationForm';
import { loans } from '../../data/loans';

afterEach(cleanup);

describe('ConsultationForm (placeholder)', () => {
  it.each(loans)('shows the locked loan and CTA for $slug', (loan) => {
    render(
      <MemoryRouter>
        <ConsultationForm defaultLoan={loan.slug} lockLoan />
      </MemoryRouter>,
    );
    expect(screen.getByTestId('consultation-locked-loan')).toHaveTextContent(loan.title);
    expect(screen.getByRole('link', { name: 'Request a consultation' })).toHaveAttribute(
      'href',
      `/consultation?loan=${loan.slug}`,
    );
  });
});
