import Badge from '../ui/Badge';
import Disclaimer from '../ui/Disclaimer';
import { formatINRCompact, formatTenure } from '../../lib/format';
import type { Loan } from '../../types';

export default function LoanHeader({ loan }: { loan: Loan }) {
  return (
    <header className="space-y-4">
      <h1 className="text-3xl font-bold text-text md:text-4xl">{loan.title}</h1>
      <p className="text-lg text-muted">{loan.intro}</p>
      <ul className="flex flex-wrap gap-2">
        <li>
          <Badge>{`Rates from ${loan.rateRange.min}% p.a.`}</Badge>
        </li>
        <li>
          <Badge>{`Up to ${formatINRCompact(loan.amountRange.max)}`}</Badge>
        </li>
        <li>
          <Badge>{`Tenure up to ${formatTenure(loan.tenureMonths.max)}`}</Badge>
        </li>
      </ul>
      <Disclaimer>
        Rates shown are indicative and subject to the lender&apos;s policy and your credit profile.
      </Disclaimer>
    </header>
  );
}
