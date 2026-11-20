import { Link } from 'react-router-dom';
import { ArrowRightLeft, Briefcase, Building2, Car, Coins, Home, Landmark, User } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import Section from '../ui/Section';
import Card from '../ui/Card';
import Disclaimer from '../ui/Disclaimer';
import { loans } from '../../data/loans';

const ICONS: Record<string, LucideIcon> = {
  User,
  Home,
  Briefcase,
  Building2,
  Coins,
  Car,
  ArrowRightLeft,
};

export default function LoanGrid() {
  return (
    <Section aria-labelledby="loans-heading">
      <h2 id="loans-heading" className="mb-8 text-2xl font-bold text-text md:text-3xl">
        Our loan products
      </h2>
      <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {loans.map((loan) => {
          const Icon = ICONS[loan.icon] ?? Landmark;
          return (
            <li key={loan.slug}>
              <Card className="flex h-full flex-col">
                <Icon aria-hidden="true" className="h-8 w-8 text-primary" />
                <h3 className="mt-3 text-lg font-semibold text-text">{loan.title}</h3>
                <p className="mt-1 flex-1 text-base text-muted">{loan.shortDescription}</p>
                <p className="mt-3 text-sm font-medium text-text">
                  From {loan.rateRange.min}% p.a. (indicative)
                </p>
                <Link
                  to={`/loans/${loan.slug}`}
                  aria-label={`Learn more about ${loan.title}`}
                  className="mt-3 inline-flex min-h-[44px] items-center font-semibold text-primary underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
                >
                  Learn more
                </Link>
              </Card>
            </li>
          );
        })}
      </ul>
      <div className="mt-6">
        <Disclaimer>
          Rates shown are indicative and subject to the lender&apos;s policy and your credit profile.
        </Disclaimer>
      </div>
    </Section>
  );
}
