import { Link } from 'react-router-dom';
import Section from '../ui/Section';
import { partners } from '../../data/partners';

export default function Partners() {
  return (
    <Section aria-labelledby="partners-heading">
      <h2 id="partners-heading" className="mb-6 text-2xl font-bold text-text md:text-3xl">
        Our lending partners
      </h2>
      <ul className="flex items-center gap-6 overflow-x-auto pb-2">
        {partners.map((p) => (
          <li key={p.name} className="shrink-0">
            <img src={p.logo} alt={p.name} loading="lazy" className="h-12 w-auto" />
          </li>
        ))}
      </ul>
      <p className="mt-4 text-sm text-muted">
        Indicative list.{' '}
        <Link
          to="/partners"
          className="font-medium text-primary underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
        >
          See all partners
        </Link>
      </p>
    </Section>
  );
}
