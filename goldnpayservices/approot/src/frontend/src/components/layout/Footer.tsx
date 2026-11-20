import { Link } from 'react-router-dom';
import { loans } from '../../data/loans';
import { site } from '../../config/site';

const linkClass =
  'rounded text-sm text-text hover:text-primary hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2';
const headingClass = 'mb-3 font-heading text-base font-semibold text-text';

const socials: Array<[string, string]> = [
  ['LinkedIn', site.social.linkedin],
  ['Instagram', site.social.instagram],
  ['Facebook', site.social.facebook],
];

export default function Footer() {
  const waHref = `https://wa.me/${site.whatsapp.replace(/\D/g, '')}`;
  return (
    <footer className="border-t border-surface bg-surface">
      <div className="mx-auto max-w-7xl px-4 py-12">
        <div className="grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-4">
          <div>
            <h2 className={headingClass}>{site.name}</h2>
            <p className="text-sm text-text">{site.tagline}</p>
          </div>
          <div>
            <h2 className={headingClass}>Loans</h2>
            <ul className="space-y-2">
              {loans.map((loan) => (
                <li key={loan.slug}>
                  <Link to={`/loans/${loan.slug}`} className={linkClass}>
                    {loan.title}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
          <div>
            <h2 className={headingClass}>Tools and Company</h2>
            <ul className="space-y-2">
              <li><Link to="/emi-calculator" className={linkClass}>EMI Calculator</Link></li>
              <li><Link to="/eligibility" className={linkClass}>Eligibility Calculator</Link></li>
              <li><Link to="/faq" className={linkClass}>FAQ</Link></li>
              <li><Link to="/about" className={linkClass}>About Us</Link></li>
              <li><Link to="/contact" className={linkClass}>Contact Us</Link></li>
            </ul>
          </div>
          <div>
            <h2 className={headingClass}>Contact</h2>
            <ul className="space-y-2 text-sm text-text">
              <li><a href={`tel:${site.phoneE164}`} className={linkClass}>{site.phone}</a></li>
              <li><a href={`mailto:${site.email}`} className={linkClass}>{site.email}</a></li>
              <li>
                <a href={waHref} target="_blank" rel="noopener noreferrer" className={linkClass}>
                  WhatsApp
                </a>
              </li>
              <li>{site.address}</li>
              <li>{site.hours}</li>
              {socials
                .filter(([, url]) => url !== '')
                .map(([name, url]) => (
                  <li key={name}>
                    <a href={url} target="_blank" rel="noopener noreferrer" className={linkClass}>
                      {name}
                    </a>
                  </li>
                ))}
            </ul>
          </div>
        </div>
        <p className="mt-10 rounded-xl border border-muted bg-background p-4 text-sm text-text">
          {site.name} is a loan consultant and not a lender. Loans are sanctioned at the sole
          discretion of our partner banks and NBFCs. Interest rates shown are indicative and subject
          to the lender&apos;s policy and your credit profile.
        </p>
        <div className="mt-6 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <ul className="flex flex-wrap gap-x-6 gap-y-2">
            <li><Link to="/privacy" className={linkClass}>Privacy</Link></li>
            <li><Link to="/terms" className={linkClass}>Terms</Link></li>
            <li><Link to="/disclaimer" className={linkClass}>Disclaimer</Link></li>
            <li><Link to="/partners" className={linkClass}>Partners</Link></li>
          </ul>
          <p className="text-sm text-text">
            &copy; {new Date().getFullYear()} {site.name}
          </p>
        </div>
      </div>
    </footer>
  );
}
