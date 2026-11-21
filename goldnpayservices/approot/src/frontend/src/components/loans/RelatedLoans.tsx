import { Link } from 'react-router-dom';
import Card from '../ui/Card';
import { getRelatedLoans } from './getRelatedLoans';
import type { Loan } from '../../types';

export default function RelatedLoans({ loan }: { loan: Loan }) {
  return (
    <section aria-labelledby="related-loans-heading" className="space-y-3">
      <h2 id="related-loans-heading" className="text-2xl font-bold text-text">
        Related loans
      </h2>
      <ul className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        {getRelatedLoans(loan).map((l) => (
          <li key={l.slug}>
            <Card className="h-full">
              <h3 className="text-lg font-semibold text-text">{l.title}</h3>
              <p className="mt-1 text-base text-muted">{l.shortDescription}</p>
              <Link
                to={`/loans/${l.slug}`}
                aria-label={`Learn more about ${l.title}`}
                className="mt-3 inline-flex min-h-[44px] items-center font-semibold text-primary underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
              >
                Learn more
              </Link>
            </Card>
          </li>
        ))}
      </ul>
    </section>
  );
}
