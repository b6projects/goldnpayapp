import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import EligibilityChecker from './EligibilityChecker';

afterEach(cleanup);

function renderChecker() {
  render(
    <MemoryRouter>
      <EligibilityChecker />
    </MemoryRouter>,
  );
}

function fill(income: string, emis = '0') {
  fireEvent.change(screen.getByLabelText(/income/i), { target: { value: income } });
  fireEvent.change(screen.getByLabelText('Existing EMIs'), { target: { value: emis } });
}

const submit = () => fireEvent.click(screen.getByRole('button', { name: 'Check eligibility' }));

const DISCLAIMER =
  "Indicative estimate only. Actual eligibility depends on the lender's assessment, your credit score and other factors.";

describe('EligibilityChecker', () => {
  it('shows no result before submit', () => {
    renderChecker();
    expect(screen.queryByText(/may be eligible/i)).toBeNull();
    expect(screen.getByText('Assumed rate 11% p.a. (indicative)')).toBeInTheDocument();
  });

  it('computes the max EMI for a salaried applicant (loan amount vector is in eligibility.test.ts)', () => {
    renderChecker();
    fill('100000', '10000');
    fireEvent.change(screen.getByLabelText('Tenure in months'), { target: { value: '60' } });
    submit();
    expect(screen.getByText('Maximum affordable EMI: ₹40,000')).toBeInTheDocument();
    expect(screen.getByText(/You may be eligible for up to ₹\d/)).toBeInTheDocument();
  });

  it('uses FOIR 0.45 and the ITR label for self-employed applicants', () => {
    renderChecker();
    fireEvent.change(screen.getByLabelText('Employment type'), {
      target: { value: 'self-employed' },
    });
    expect(screen.getByLabelText('Average monthly income (from ITR)')).toBeInTheDocument();
    fill('100000', '0');
    submit();
    expect(screen.getByText('Maximum affordable EMI: ₹45,000')).toBeInTheDocument();
  });

  it('rounds the amount down to a multiple of 10,000 and links the CTA', () => {
    renderChecker();
    fill('100000', '10000');
    submit();
    const heading = screen.getByText(/You may be eligible for up to ₹/);
    const digits = Number(heading.textContent?.replace(/\D/g, ''));
    expect(digits).toBeGreaterThan(0);
    expect(digits % 10000).toBe(0);
    expect(screen.getByRole('link', { name: 'Talk to an expert' })).toHaveAttribute(
      'href',
      `/consultation?loan=personal-loan&amount=${digits}`,
    );
    expect(screen.getByText(DISCLAIMER)).toBeInTheDocument();
  });

  it('shows the zero-result message without "up to" and a CTA without an amount', () => {
    renderChecker();
    fill('20000', '15000');
    submit();
    expect(
      screen.getByText(
        'Your existing EMIs are high relative to income. Talk to us about balance transfer or consolidation options.',
      ),
    ).toBeInTheDocument();
    expect(screen.queryByText(/up to/i)).toBeNull();
    expect(screen.getByText(DISCLAIMER)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Talk to an expert' })).toHaveAttribute(
      'href',
      '/consultation?loan=personal-loan',
    );
  });

  it('updates the assumed rate, tenure and CTA slug when the loan type changes', () => {
    renderChecker();
    fireEvent.change(screen.getByLabelText('Loan type'), { target: { value: 'home-loan' } });
    expect(screen.getByText('Assumed rate 8.5% p.a. (indicative)')).toBeInTheDocument();
    expect(screen.getByLabelText('Tenure in months')).toHaveValue('240');
    fill('150000', '0');
    submit();
    expect(screen.getByRole('link', { name: 'Talk to an expert' }).getAttribute('href')).toMatch(
      /^\/consultation\?loan=home-loan&amount=\d+$/,
    );
  });

  it('requires income', () => {
    renderChecker();
    submit();
    expect(screen.getByText('Enter your monthly income')).toBeInTheDocument();
    expect(screen.queryByText(/may be eligible/i)).toBeNull();
  });

  it('validates existing EMIs and tenure', () => {
    renderChecker();
    fill('100000', '-5');
    fireEvent.change(screen.getByLabelText('Tenure in months'), { target: { value: '500' } });
    submit();
    expect(screen.getByText('Existing EMIs must be 0 or more')).toBeInTheDocument();
    expect(screen.getByText('Enter a tenure between 12 and 60 months')).toBeInTheDocument();
  });

  it('rejects an absurdly large income', () => {
    renderChecker();
    fill('9999999999999');
    submit();
    expect(screen.getByText('Enter a monthly income below ₹100 crore')).toBeInTheDocument();
  });

  it('hides a stale result once an input changes', () => {
    renderChecker();
    fill('100000', '0');
    submit();
    expect(screen.getByText(/may be eligible/i)).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('Existing EMIs'), { target: { value: '5000' } });
    expect(screen.queryByText(/may be eligible/i)).toBeNull();
  });
});
