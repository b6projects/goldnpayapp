import { useState } from 'react';
import { Link } from 'react-router-dom';
import Card from '../ui/Card';
import Slider from '../ui/Slider';
import Disclaimer from '../ui/Disclaimer';
import { calculateEmi } from '../../lib/emi';
import { formatINR, formatINRCompact, formatTenure } from '../../lib/format';

export const QUICK_EMI_RATE = 11;

export default function QuickEmiCard() {
  const [amount, setAmount] = useState(500000);
  const [months, setMonths] = useState(36);
  const emi = calculateEmi(amount, QUICK_EMI_RATE, months);

  return (
    <Card className="space-y-5" aria-labelledby="quick-emi-heading">
      <h2 id="quick-emi-heading" className="text-xl font-bold text-text">
        Quick EMI estimate
      </h2>
      <Slider
        label={`Loan amount: ${formatINRCompact(amount)}`}
        value={amount}
        min={100000}
        max={5000000}
        step={50000}
        onChange={setAmount}
        valueText={formatINRCompact(amount)}
      />
      <Slider
        label={`Tenure: ${formatTenure(months)}`}
        value={months}
        min={12}
        max={84}
        step={6}
        onChange={setMonths}
        valueText={formatTenure(months)}
      />
      <p className="text-sm text-muted">Rate: {QUICK_EMI_RATE}% p.a. (indicative)</p>
      <div>
        <p className="text-sm text-muted">Indicative monthly EMI</p>
        <p className="text-3xl font-bold text-primary" data-testid="quick-emi-value">
          {formatINR(emi)}
        </p>
      </div>
      <Disclaimer>Indicative estimate. Actual rate and EMI depend on the lender.</Disclaimer>
      <Link
        to="/emi-calculator"
        className="inline-flex min-h-[44px] items-center font-semibold text-primary underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
      >
        Detailed calculator
      </Link>
    </Card>
  );
}
