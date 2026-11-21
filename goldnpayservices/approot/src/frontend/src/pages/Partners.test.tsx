import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import Partners from './Partners';
import { partners } from '../data/partners';

afterEach(cleanup);

describe('Partners page', () => {
  it('renders the exact intro and a Name/Type table with one row per partner', () => {
    render(<Partners />);
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    expect(
      screen.getByText(
        'We work with the following regulated entities. The final decision on any loan rests solely with the lender.',
      ),
    ).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: 'Name' })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: 'Type' })).toBeInTheDocument();
    expect(screen.getAllByRole('row')).toHaveLength(partners.length + 1);
    expect(screen.getByText('State Bank of India')).toBeInTheDocument();
    expect(screen.getByText('HDFC Bank')).toBeInTheDocument();
  });
});
