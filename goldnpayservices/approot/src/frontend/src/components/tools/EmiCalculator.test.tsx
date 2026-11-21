import { afterEach, describe, expect, it, vi } from 'vitest';
import { act, cleanup, fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import EmiCalculator from './EmiCalculator';
import { loans } from '../../data/loans';
import { calculateEmi, summarize } from '../../lib/emi';
import { formatINR } from '../../lib/format';
import type { Loan } from '../../types';

vi.mock('./EmiChart', () => ({ default: () => null }));

afterEach(() => {
  cleanup();
  vi.useRealTimers();
});

function renderCalc(props: { loan?: Loan; allowLoanSwitch?: boolean } = {}) {
  return render(
    <MemoryRouter>
      <EmiCalculator {...props} />
    </MemoryRouter>,
  );
}

const amountSlider = () => screen.getByLabelText('Loan amount');
const rateSlider = () => screen.getByLabelText('Interest rate (% p.a.)');
const tenureSlider = () => screen.getByLabelText('Tenure');
const amountField = () => screen.getByLabelText('Loan amount value');
const rateField = () => screen.getByLabelText('Interest rate (% p.a.) value');
const tenureField = () => screen.getByLabelText('Tenure value');
const emi = () => screen.getByTestId('emi-value');

describe('EmiCalculator', () => {
  it.each(loans)('starts at the defaults and limits sliders to the ranges for $slug', (loan) => {
    renderCalc({ loan });
    const { amount, rate, tenureMonths } = loan.defaults;
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(amount, rate, tenureMonths)));
    const sliders = screen.getAllByRole('slider');
    expect(sliders).toEqual([amountSlider(), rateSlider(), tenureSlider()]);
    expect(amountSlider()).toHaveAttribute('min', String(loan.amountRange.min));
    expect(amountSlider()).toHaveAttribute('max', String(loan.amountRange.max));
    expect(rateSlider()).toHaveAttribute('min', String(loan.rateRange.min));
    expect(rateSlider()).toHaveAttribute('max', String(loan.rateRange.max));
    expect(rateSlider()).toHaveAttribute('step', '0.05');
    expect(tenureSlider()).toHaveAttribute('min', String(loan.tenureMonths.min));
    expect(tenureSlider()).toHaveAttribute('max', String(loan.tenureMonths.max));
    expect(amountField()).toHaveValue(String(amount));
    expect(screen.getAllByText(/indicative/i).length).toBeGreaterThan(0);
  });

  it('keeps the amount slider and number field in sync and updates the EMI', () => {
    renderCalc({ loan: loans[0] });
    fireEvent.change(amountSlider(), { target: { value: '800000' } });
    expect(amountField()).toHaveValue('800000');
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(800000, 11, 36)));

    fireEvent.change(amountField(), { target: { value: '900000' } });
    expect(amountSlider()).toHaveValue('900000');
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(900000, 11, 36)));
  });

  it('keeps the rate in sync at step 0.05', () => {
    renderCalc({ loan: loans[0] });
    fireEvent.change(rateSlider(), { target: { value: '10.55' } });
    expect(rateField()).toHaveValue('10.55');
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(500000, 10.55, 36)));

    fireEvent.change(rateField(), { target: { value: '12.25' } });
    expect(rateSlider()).toHaveValue('12.25');
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(500000, 12.25, 36)));
  });

  it('keeps the tenure in sync', () => {
    renderCalc({ loan: loans[0] });
    fireEvent.change(tenureSlider(), { target: { value: '48' } });
    expect(tenureField()).toHaveValue('48');
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(500000, 11, 48)));

    fireEvent.change(tenureField(), { target: { value: '24' } });
    expect(tenureSlider()).toHaveValue('24');
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(500000, 11, 24)));
  });

  it('clamps an out-of-range entry on blur and explains why', () => {
    renderCalc({ loan: loans[0] });
    fireEvent.change(amountField(), { target: { value: '99999999' } });
    fireEvent.blur(amountField());
    expect(amountField()).toHaveValue('4000000');
    expect(
      screen.getByText('Amount adjusted to the allowed range: ₹50,000 to ₹40,00,000'),
    ).toBeInTheDocument();
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(4000000, 11, 36)));

    fireEvent.change(amountField(), { target: { value: '5' } });
    fireEvent.keyDown(amountField(), { key: 'Enter' });
    expect(amountField()).toHaveValue('50000');
  });

  it('reverts a non-numeric entry on blur', () => {
    renderCalc({ loan: loans[0] });
    fireEvent.change(amountField(), { target: { value: 'abc' } });
    fireEvent.blur(amountField());
    expect(amountField()).toHaveValue('500000');
  });

  it('uses the spec ranges when no loan is passed', () => {
    renderCalc();
    expect(amountSlider()).toHaveAttribute('min', '10000');
    expect(amountSlider()).toHaveAttribute('max', '100000000');
    expect(rateSlider()).toHaveAttribute('min', '1');
    expect(rateSlider()).toHaveAttribute('max', '36');
    expect(tenureSlider()).toHaveAttribute('min', '1');
    expect(tenureSlider()).toHaveAttribute('max', '360');
  });

  it('converts the tenure when switching between Months and Years', () => {
    renderCalc({ loan: loans[0] });
    const before = emi().textContent;
    const years = screen.getByRole('button', { name: 'Years' });
    const monthsBtn = screen.getByRole('button', { name: 'Months' });
    expect(monthsBtn).toHaveAttribute('aria-pressed', 'true');
    fireEvent.click(years);
    expect(years).toHaveAttribute('aria-pressed', 'true');
    expect(tenureSlider()).toHaveAttribute('min', '1');
    expect(tenureSlider()).toHaveAttribute('max', '5');
    expect(tenureField()).toHaveValue('3');
    expect(emi().textContent).toBe(before);
    fireEvent.change(tenureSlider(), { target: { value: '5' } });
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(500000, 11, 60)));
    fireEvent.click(monthsBtn);
    expect(tenureField()).toHaveValue('60');
    expect(tenureSlider()).toHaveAttribute('max', '60');
  });

  it('shows principal, interest, total and the percentage text alternative', () => {
    renderCalc({ loan: loans[0] });
    const s = summarize(500000, 11, 36);
    expect(screen.getByTestId('principal-value')).toHaveTextContent('₹5,00,000');
    expect(screen.getByTestId('interest-value')).toHaveTextContent(formatINR(s.totalInterest));
    expect(screen.getByTestId('total-value')).toHaveTextContent(formatINR(s.totalPayment));
    const principalPct = Math.round((500000 / s.totalPayment) * 100);
    const text = screen.getByText(`Principal ${principalPct}%, interest ${100 - principalPct}%`);
    expect(screen.getByRole('img', { name: /chart/i })).toHaveAttribute(
      'aria-describedby',
      text.id,
    );
  });

  it('links the CTA to the consultation page with the current amount', () => {
    renderCalc({ loan: loans[0] });
    const cta = () => screen.getByRole('link', { name: 'Get best offers for this loan' });
    expect(cta()).toHaveAttribute('href', '/consultation?loan=personal-loan&amount=500000');
    fireEvent.change(amountField(), { target: { value: '750000' } });
    expect(cta()).toHaveAttribute('href', '/consultation?loan=personal-loan&amount=750000');
  });

  it('omits the loan from the CTA when no loan is passed', () => {
    renderCalc();
    expect(screen.getByRole('link', { name: 'Get best offers for this loan' })).toHaveAttribute(
      'href',
      '/consultation?amount=500000',
    );
  });

  it('lets the user switch loans and resets values when allowLoanSwitch is set', () => {
    renderCalc({ loan: loans[0], allowLoanSwitch: true });
    const select = screen.getByLabelText('Loan type');
    expect(within(select).getAllByRole('option')).toHaveLength(loans.length);
    const home = loans.find((l) => l.slug === 'home-loan') as Loan;
    fireEvent.change(select, { target: { value: 'home-loan' } });
    expect(amountSlider()).toHaveAttribute('min', String(home.amountRange.min));
    expect(tenureSlider()).toHaveAttribute('max', String(home.tenureMonths.max));
    const { amount, rate, tenureMonths } = home.defaults;
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(amount, rate, tenureMonths)));
    expect(screen.getByRole('link', { name: 'Get best offers for this loan' })).toHaveAttribute(
      'href',
      `/consultation?loan=home-loan&amount=${amount}`,
    );
  });

  it('does not show a loan selector without allowLoanSwitch', () => {
    renderCalc({ loan: loans[0] });
    expect(screen.queryByLabelText('Loan type')).toBeNull();
  });

  it('defaults the selector to the personal loan when no loan is passed', () => {
    renderCalc({ allowLoanSwitch: true });
    expect(screen.getByLabelText('Loan type')).toHaveValue('personal-loan');
    expect(amountSlider()).toHaveAttribute('max', '4000000');
  });

  it('debounces the repayment schedule by 150 ms while the EMI updates immediately', () => {
    vi.useFakeTimers();
    renderCalc({ loan: loans[0] });
    fireEvent.click(screen.getByRole('button', { name: 'View repayment schedule' }));
    const yearRows = () => screen.queryAllByRole('button', { name: /^Year \d+$/ });
    expect(yearRows()).toHaveLength(3);

    fireEvent.change(tenureSlider(), { target: { value: '60' } });
    expect(emi()).toHaveTextContent(formatINR(calculateEmi(500000, 11, 60)));
    expect(yearRows()).toHaveLength(3);
    act(() => {
      vi.advanceTimersByTime(149);
    });
    expect(yearRows()).toHaveLength(3);
    act(() => {
      vi.advanceTimersByTime(1);
    });
    expect(yearRows()).toHaveLength(5);
  });
});
