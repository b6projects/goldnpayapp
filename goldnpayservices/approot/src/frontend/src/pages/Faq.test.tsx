import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Faq from './Faq';
import { faqs } from '../data/faqs';

afterEach(cleanup);

function renderFaq() {
  return render(
    <MemoryRouter>
      <Faq />
    </MemoryRouter>,
  );
}

describe('Faq page', () => {
  it('renders one h1 and a heading per category', () => {
    renderFaq();
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    for (const c of ['General', 'Process', 'Fees', 'Eligibility']) {
      expect(screen.getByRole('heading', { level: 2, name: c })).toBeInTheDocument();
    }
    expect(screen.getByRole('status')).toHaveTextContent(`${faqs.length} questions found`);
  });

  it('filters by search and announces the count', () => {
    renderFaq();
    fireEvent.change(screen.getByLabelText('Search questions'), { target: { value: 'lender' } });
    expect(screen.getByText('Are you a lender?')).toBeInTheDocument();
    expect(screen.queryByText('How does the process work?')).not.toBeInTheDocument();
    expect(screen.queryByRole('heading', { level: 2, name: 'Process' })).not.toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent(/questions? found/);
  });

  it('shows a message when nothing matches and restores on clear', () => {
    renderFaq();
    const input = screen.getByLabelText('Search questions');
    fireEvent.change(input, { target: { value: 'zzzzqqq' } });
    expect(screen.getByRole('status')).toHaveTextContent('No questions match your search');
    fireEvent.change(input, { target: { value: '' } });
    expect(screen.getByText('How does the process work?')).toBeInTheDocument();
    expect(screen.getByText('Do you charge customers a fee?')).toBeInTheDocument();
  });
});
