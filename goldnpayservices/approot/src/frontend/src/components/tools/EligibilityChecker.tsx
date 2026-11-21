import { useState } from 'react';
import type { FormEvent } from 'react';
import Card from '../ui/Card';
import Input from '../ui/Input';
import Select from '../ui/Select';
import Button from '../ui/Button';
import Disclaimer from '../ui/Disclaimer';
import LinkButton from '../ui/LinkButton';
import { maxEligibleLoan, roundDownEligible } from '../../lib/eligibility';
import { buildConsultationPath } from '../../lib/consultation';
import { formatINR } from '../../lib/format';
import { loans } from '../../data/loans';

type Employment = 'salaried' | 'self-employed';

const FOIR: Record<Employment, number> = { salaried: 0.5, 'self-employed': 0.45 };
const MAX_INCOME = 1_000_000_000; // 100 crore per month sanity cap

interface Result {
  slug: string;
  maxEmi: number;
  amount: number;
}

interface Errors {
  income?: string;
  emis?: string;
  tenure?: string;
}

const DISCLAIMER_TEXT =
  "Indicative estimate only. Actual eligibility depends on the lender's assessment, your credit score and other factors.";

export default function EligibilityChecker() {
  const [employment, setEmployment] = useState<Employment>('salaried');
  const [income, setIncome] = useState('');
  const [emis, setEmis] = useState('0');
  const [slug, setSlug] = useState('personal-loan');
  const [tenure, setTenure] = useState(() => String(loans[0].defaults.tenureMonths));
  const [errors, setErrors] = useState<Errors>({});
  const [result, setResult] = useState<Result | null>(null);

  const loan = loans.find((l) => l.slug === slug) ?? loans[0];
  const errorList = Object.values(errors).filter(Boolean);

  const edit =
    <T,>(setter: (v: T) => void) =>
    (v: T) => {
      setter(v);
      setResult(null);
    };

  const changeLoan = (next: string) => {
    const nextLoan = loans.find((l) => l.slug === next) ?? loans[0];
    setSlug(nextLoan.slug);
    setTenure(String(nextLoan.defaults.tenureMonths));
    setResult(null);
    setErrors({});
  };

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    const next: Errors = {};
    const incomeValue = Number(income);
    if (income.trim() === '' || !Number.isFinite(incomeValue) || incomeValue <= 0) {
      next.income = 'Enter your monthly income';
    } else if (incomeValue > MAX_INCOME) {
      next.income = 'Enter a monthly income below ₹100 crore';
    }
    const emiValue = emis.trim() === '' ? 0 : Number(emis);
    if (!Number.isFinite(emiValue) || emiValue < 0) {
      next.emis = 'Existing EMIs must be 0 or more';
    }
    const tenureValue = Number(tenure);
    const { min, max } = loan.tenureMonths;
    if (
      tenure.trim() === '' ||
      !Number.isInteger(tenureValue) ||
      tenureValue < min ||
      tenureValue > max
    ) {
      next.tenure = `Enter a tenure between ${min} and ${max} months`;
    }
    setErrors(next);
    if (Object.keys(next).length > 0) {
      setResult(null);
      return;
    }
    const { maxEmi, maxLoan } = maxEligibleLoan({
      monthlyIncome: incomeValue,
      existingEmis: emiValue,
      annualRatePct: loan.defaults.rate,
      months: tenureValue,
      foir: FOIR[employment],
    });
    setResult({ slug: loan.slug, maxEmi, amount: roundDownEligible(maxLoan) });
  };

  return (
    <Card className="space-y-6" aria-labelledby="eligibility-heading">
      <h2 id="eligibility-heading" className="text-xl font-bold text-text">
        Check your loan eligibility
      </h2>
      <form onSubmit={onSubmit} noValidate className="space-y-4">
        <Select
          label="Employment type"
          value={employment}
          onChange={(e) => edit(setEmployment)(e.target.value as Employment)}
        >
          <option value="salaried">Salaried</option>
          <option value="self-employed">Self-employed</option>
        </Select>
        <Input
          label={
            employment === 'self-employed'
              ? 'Average monthly income (from ITR)'
              : 'Monthly net income'
          }
          inputMode="numeric"
          value={income}
          error={errors.income}
          onChange={(e) => edit(setIncome)(e.target.value)}
        />
        <Input
          label="Existing EMIs"
          inputMode="numeric"
          value={emis}
          error={errors.emis}
          onChange={(e) => edit(setEmis)(e.target.value)}
        />
        <Select label="Loan type" value={slug} onChange={(e) => changeLoan(e.target.value)}>
          {loans.map((l) => (
            <option key={l.slug} value={l.slug}>
              {l.title}
            </option>
          ))}
        </Select>
        <Input
          label="Tenure in months"
          inputMode="numeric"
          value={tenure}
          error={errors.tenure}
          onChange={(e) => edit(setTenure)(e.target.value)}
        />
        <p className="text-sm text-muted">Assumed rate {loan.defaults.rate}% p.a. (indicative)</p>
        <Button type="submit">Check eligibility</Button>
      </form>

      <div aria-live="polite" data-testid="eligibility-result">
        {errorList.length > 0 && (
          <p className="text-sm text-red-700">
            Please fix {errorList.length} {errorList.length === 1 ? 'error' : 'errors'} above.
          </p>
        )}
        {result && result.amount > 0 && (
          <div className="space-y-3 rounded-xl bg-surface p-4">
            <h3 className="text-lg font-bold text-primary">
              You may be eligible for up to {formatINR(result.amount)}
            </h3>
            <p className="text-text">Maximum affordable EMI: {formatINR(result.maxEmi)}</p>
            <Disclaimer>{DISCLAIMER_TEXT}</Disclaimer>
            <LinkButton to={buildConsultationPath({ loan: result.slug, amount: result.amount })}>
              Talk to an expert
            </LinkButton>
          </div>
        )}
        {result && result.amount === 0 && (
          <div className="space-y-3 rounded-xl bg-surface p-4">
            <p className="text-text">
              Your existing EMIs are high relative to income. Talk to us about balance transfer or
              consolidation options.
            </p>
            <Disclaimer>{DISCLAIMER_TEXT}</Disclaimer>
            <LinkButton to={buildConsultationPath({ loan: result.slug })}>
              Talk to an expert
            </LinkButton>
          </div>
        )}
      </div>
    </Card>
  );
}
