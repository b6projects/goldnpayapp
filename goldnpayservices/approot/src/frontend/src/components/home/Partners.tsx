import { Link } from 'react-router-dom';
import Section from '../ui/Section';
import BankBadge from '../ui/BankBadge';
import { partners } from '../../data/partners';

export default function Partners() {
  return (
    <Section aria-labelledby="partners-heading">
      <h2 id="partners-heading" className="heading-accent mb-6 text-2xl font-bold text-text md:text-3xl">
        Our lending partners
      </h2>
      <div className="marquee overflow-hidden [mask-image:linear-gradient(90deg,transparent,#000_5%,#000_95%,transparent)]">
        <div className="marquee-track flex w-max animate-marquee motion-reduce:animate-none">
          <ul className="flex shrink-0 items-center gap-4 pr-4">
            {partners.map((p) => (
              <li key={p.name} className="shrink-0">
                <BankBadge partner={p} />
              </li>
            ))}
          </ul>
          {/* Duplicate set for a seamless loop; hidden from assistive tech. */}
          <ul aria-hidden="true" className="flex shrink-0 items-center gap-4 pr-4">
            {partners.map((p) => (
              <li key={p.name} className="shrink-0">
                <BankBadge partner={p} />
              </li>
            ))}
          </ul>
        </div>
      </div>
      <p className="mt-4 text-sm text-muted">
        Indicative list. Bank names are shown for identification only and belong to their respective
        owners.{' '}
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
