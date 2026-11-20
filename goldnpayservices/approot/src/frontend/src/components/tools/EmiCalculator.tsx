import { useState } from 'react';
import Card from '../ui/Card';
import Slider from '../ui/Slider';
import Disclaimer from '../ui/Disclaimer';
import { calculateEmi } from '../../lib/emi';
import { formatINR, formatINRCompact, formatTenure } from '../../lib/format';
import type { Loan } from '../../types';

export interface EmiCalculatorProps {
  loan?: Loan;
  allowLoanSwitch?: boolean;
}

const FALLBACK = {
  amountRange: { min: 100000, max: 5000000 },
  tenureMonths: { min: 12, max: 84 },
  defaults: { amount: 500000, rate: 11, tenureMonths: 36 },
};

/** Placeholder with the final prop contract; replaced by the full calculator in GPS-8 (M7). */
export default function EmiCalculator({ loan }: EmiCalculatorProps) {
  const cfg = loan ?? FALLBACK;
  const [amount, setAmount] = useState(cfg.defaults.amount);
  const [months, setMonths] = useState(cfg.defaults.tenureMonths);
  const emi = calculateEmi(amount, cfg.defaults.rate, months);

  return (
    <Card className="space-y-5" aria-labelledby="emi-calc-heading">
      <h2 id="emi-calc-heading" className="text-xl font-bold text-text">
        EMI calculator
      </h2>
      <Slider
        label={`Loan amount: ${formatINRCompact(amount)}`}
        value={amount}
        min={cfg.amountRange.min}
        max={cfg.amountRange.max}
        step={10000}
        onChange={setAmount}
        valueText={formatINRCompact(amount)}
      />
      <Slider
        label={`Tenure: ${formatTenure(months)}`}
        value={months}
        min={cfg.tenureMonths.min}
        max={cfg.tenureMonths.max}
        step={1}
        onChange={setMonths}
        valueText={formatTenure(months)}
      />
      <p className="text-sm text-muted">Rate: {cfg.defaults.rate}% p.a. (indicative)</p>
      <div>
        <p className="text-sm text-muted">Indicative monthly EMI</p>
        <p className="text-3xl font-bold text-primary" data-testid="emi-value">
          {formatINR(emi)}
        </p>
      </div>
      <Disclaimer>Indicative estimate. Actual rate and EMI depend on the lender.</Disclaimer>
    </Card>
  );
}
