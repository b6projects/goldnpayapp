import Section from '../ui/Section';
import LinkButton from '../ui/LinkButton';
import QuickEmiCard from './QuickEmiCard';
import { site } from '../../config/site';

export default function Hero() {
  return (
    <Section className="bg-surface" aria-label="Introduction">
      <div className="grid items-center gap-8 lg:grid-cols-2">
        <div>
          <h1 className="text-3xl font-bold text-text md:text-5xl">{site.tagline}</h1>
          <p className="mt-4 text-lg text-muted">
            Compare offers from 20+ banks and NBFCs. Free, unbiased advice from application to
            disbursal.
          </p>
          <div className="mt-6 flex flex-col gap-3 sm:flex-row">
            <LinkButton to="/consultation" size="lg">
              Get Free Consultation
            </LinkButton>
            <LinkButton to="/eligibility" variant="secondary" size="lg">
              Check Eligibility
            </LinkButton>
          </div>
        </div>
        <div className="hidden lg:block">
          <QuickEmiCard />
        </div>
      </div>
    </Section>
  );
}
