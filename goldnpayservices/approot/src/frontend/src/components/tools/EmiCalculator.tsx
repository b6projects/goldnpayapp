import { lazy, Suspense, useId, useMemo, useState } from 'react';
import Card from '../ui/Card';
import Select from '../ui/Select';
import Disclaimer from '../ui/Disclaimer';
import LinkButton from '../ui/LinkButton';
import SliderField from './SliderField';
import AmortizationTable from './AmortizationTable';
import { summarize } from '../../lib/emi';
import { buildConsultationPath } from '../../lib/consultation';
import { formatINR, formatTenure } from '../../lib/format';
import { useDebouncedValue } from '../../hooks/useDebouncedValue';
import { loans } from '../../data/loans';
import type { Loan } from '../../types';

const EmiChart = lazy(() => import('./EmiChart'));

export interface EmiCalculatorProps {
  loan?: Loan;
  allowLoanSwitch?: boolean;
}

interface Ranges {
  amount: { min: number; max: number };
  rate: { min: number; max: number };
  months: { min: number; max: number };
}

interface Values {
  amount: number;
  rate: number;
  months: number;
}

const NO_LOAN_RANGES: Ranges = {
  amount: { min: 10_000, max: 100_000_000 },
  rate: { min: 1, max: 36 },
  months: { min: 1, max: 360 },
};
const NO_LOAN_DEFAULTS: Values = { amount: 500_000, rate: 11, months: 36 };

function rangesFor(loan?: Loan): Ranges {
  if (!loan) return NO_LOAN_RANGES;
  return { amount: loan.amountRange, rate: loan.rateRange, months: loan.tenureMonths };
}

function defaultsFor(loan?: Loan): Values {
  if (!loan) return NO_LOAN_DEFAULTS;
  return {
    amount: loan.defaults.amount,
    rate: loan.defaults.rate,
    months: loan.defaults.tenureMonths,
  };
}

const formatRate = (v: number) => `${v}%`;
const formatYears = (v: number) => `${v} ${v === 1 ? 'year' : 'years'}`;

const toggleClasses = (active: boolean) =>
  `min-h-[44px] px-4 text-sm font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary ${
    active ? 'bg-primary text-white' : 'bg-background text-text'
  }`;

export default function EmiCalculator({ loan, allowLoanSwitch = false }: EmiCalculatorProps) {
  const [slug, setSlug] = useState<string | undefined>(
    loan?.slug ?? (allowLoanSwitch ? 'personal-loan' : undefined),
  );
  const activeLoan = allowLoanSwitch ? (loans.find((l) => l.slug === slug) ?? loan) : loan;
  const ranges = rangesFor(activeLoan);

  const [values, setValues] = useState<Values>(() =>
    defaultsFor(allowLoanSwitch ? (loans.find((l) => l.slug === slug) ?? loan) : loan),
  );
  const [unit, setUnit] = useState<'months' | 'years'>('months');
  const textId = useId();

  const { amount, rate, months } = values;
  const summary = summarize(amount, rate, months);
  const principalPct = Math.round((amount / summary.totalPayment) * 100);
  const interestPct = 100 - principalPct;

  const scheduleInputs = useMemo(() => ({ amount, rate, months }), [amount, rate, months]);
  const debounced = useDebouncedValue(scheduleInputs, 150);

  const yearMin = Math.ceil(ranges.months.min / 12);
  const yearMax = Math.floor(ranges.months.max / 12);
  const yearsAvailable = yearMin <= yearMax;
  const inYears = unit === 'years' && yearsAvailable;

  const switchLoan = (next: string) => {
    const nextLoan = loans.find((l) => l.slug === next);
    setSlug(next);
    setValues(defaultsFor(nextLoan));
    setUnit('months');
  };

  const switchUnit = (next: 'months' | 'years') => {
    if (next === 'years') {
      const years = Math.min(yearMax, Math.max(yearMin, Math.round(months / 12)));
      setValues((v) => ({ ...v, months: years * 12 }));
    }
    setUnit(next);
  };

  return (
    <Card className="space-y-4 p-4 lg:p-5" aria-labelledby="emi-calc-heading">
      <h2 id="emi-calc-heading" className="text-lg font-bold text-text">
        EMI calculator
      </h2>

      <div className="grid gap-4 lg:grid-cols-2 lg:gap-8">
        <div className="space-y-3">
          {allowLoanSwitch && (
            <Select label="Loan type" value={slug} onChange={(e) => switchLoan(e.target.value)}>
              {loans.map((l) => (
                <option key={l.slug} value={l.slug}>
                  {l.title}
                </option>
              ))}
            </Select>
          )}

          <div className="grid gap-3 sm:grid-cols-2">
            <SliderField
              label="Loan amount"
              name="Amount"
              value={amount}
              min={ranges.amount.min}
              max={ranges.amount.max}
              step={10_000}
              format={formatINR}
              onChange={(v) => setValues((s) => ({ ...s, amount: v }))}
            />
            <SliderField
              label="Interest rate (% p.a.)"
              name="Rate"
              value={rate}
              min={ranges.rate.min}
              max={ranges.rate.max}
              step={0.05}
              decimals={2}
              format={formatRate}
              onChange={(v) => setValues((s) => ({ ...s, rate: v }))}
            />
          </div>
          <div className="grid items-start gap-3 sm:grid-cols-2">
            <div className="space-y-1">
              <span className="block text-sm font-medium text-text">Tenure unit</span>
              <div
                role="group"
                aria-label="Tenure unit"
                className="inline-flex overflow-hidden rounded-xl border border-muted"
              >
                <button
                  type="button"
                  aria-pressed={!inYears}
                  className={toggleClasses(!inYears)}
                  onClick={() => switchUnit('months')}
                >
                  Months
                </button>
                <button
                  type="button"
                  aria-pressed={inYears}
                  disabled={!yearsAvailable}
                  className={`${toggleClasses(inYears)} disabled:opacity-50`}
                  onClick={() => switchUnit('years')}
                >
                  Years
                </button>
              </div>
            </div>
            {inYears ? (
              <SliderField
                key="years"
                label="Tenure"
                name="Tenure"
                value={Math.round(months / 12)}
                min={yearMin}
                max={yearMax}
                step={1}
                format={formatYears}
                onChange={(v) => setValues((s) => ({ ...s, months: v * 12 }))}
              />
            ) : (
              <SliderField
                key="months"
                label="Tenure"
                name="Tenure"
                value={months}
                min={ranges.months.min}
                max={ranges.months.max}
                step={1}
                format={formatTenure}
                onChange={(v) => setValues((s) => ({ ...s, months: v }))}
              />
            )}
          </div>
        </div>

        <div className="space-y-3">
          <div className="text-center">
            <h3 className="sr-only">Results</h3>
            <p className="text-sm text-muted">Monthly EMI (indicative)</p>
            <p className="text-3xl font-bold text-primary" data-testid="emi-value">
              {formatINR(summary.emi)}
            </p>
            <dl className="mt-2 grid grid-cols-3 gap-1 sm:gap-2">
              <div>
                <dt className="text-xs text-muted sm:text-sm">Principal</dt>
                <dd
                  className="text-sm font-semibold text-text sm:text-base"
                  data-testid="principal-value"
                >
                  {formatINR(amount)}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted sm:text-sm">Total interest</dt>
                <dd
                  className="text-sm font-semibold text-text sm:text-base"
                  data-testid="interest-value"
                >
                  {formatINR(summary.totalInterest)}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted sm:text-sm">Total payment</dt>
                <dd
                  className="text-sm font-semibold text-text sm:text-base"
                  data-testid="total-value"
                >
                  {formatINR(summary.totalPayment)}
                </dd>
              </div>
            </dl>
          </div>

          <div>
            <div role="img" aria-label="Principal versus interest chart" aria-describedby={textId}>
              <Suspense fallback={<div aria-hidden="true" className="h-[210px]" />}>
                <EmiChart principal={amount} interest={summary.totalInterest} />
              </Suspense>
            </div>
            <p id={textId} className="text-center text-sm text-text">
              Principal {principalPct}%, interest {interestPct}%
            </p>
          </div>

          <div className="flex justify-center">
            <LinkButton to={buildConsultationPath({ loan: activeLoan?.slug, amount })}>
              Get best offers for this loan
            </LinkButton>
          </div>
        </div>
      </div>

      <AmortizationTable
        principal={debounced.amount}
        ratePct={debounced.rate}
        months={debounced.months}
        loanTitle={activeLoan?.title}
      />

      <Disclaimer>
        Indicative estimate. Actual rate and EMI depend on the lender. Rates, EMI and amounts shown
        here are indicative only.
      </Disclaimer>
    </Card>
  );
}
