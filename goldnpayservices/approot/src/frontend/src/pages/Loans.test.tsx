import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Loans from './Loans';

afterEach(cleanup);

describe('Loans page', () => {
  it('renders H1, intro, 7 cards and the disclaimer', () => {
    render(
      <MemoryRouter>
        <Loans />
      </MemoryRouter>,
    );
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    expect(screen.getByRole('heading', { level: 1, name: 'Loan Types' })).toBeInTheDocument();
    expect(screen.getByText(/Compare the loan types/)).toBeInTheDocument();
    expect(screen.getAllByRole('listitem')).toHaveLength(7);
    expect(screen.getByText(/Rates shown are indicative/)).toBeInTheDocument();
  });
});
