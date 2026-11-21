import Section from '../ui/Section';
import LinkButton from '../ui/LinkButton';
import QuickEmiCard from './QuickEmiCard';
import { site } from '../../config/site';

export default function Hero() {
  return (
    <Section className="hero-gradient relative overflow-hidden" aria-label="Introduction">
      <div aria-hidden="true" className="pointer-events-none absolute inset-0">
        <div className="absolute -left-24 -top-24 h-72 w-72 animate-blob rounded-full bg-sand/50 blur-3xl" />
        <div
          className="absolute -bottom-32 right-0 h-96 w-96 animate-blob rounded-full bg-accent/20 blur-3xl"
          style={{ animationDelay: '-6s' }}
        />
      </div>
      <div className="relative grid items-center gap-8 lg:grid-cols-2">
        <div>
          <h1 className="animate-fade-up text-3xl font-bold text-text md:text-5xl">
            {site.tagline}
          </h1>
          <p
            className="mt-4 animate-fade-up text-lg text-muted"
            style={{ animationDelay: '150ms' }}
          >
            Compare offers from 20+ banks and NBFCs. Free, unbiased advice from application to
            disbursal.
          </p>
          <div
            className="mt-6 flex animate-fade-up flex-col gap-3 sm:flex-row"
            style={{ animationDelay: '300ms' }}
          >
            <LinkButton to="/consultation" size="lg">
              Get Free Consultation
            </LinkButton>
            <LinkButton to="/eligibility" variant="secondary" size="lg">
              Check Eligibility
            </LinkButton>
          </div>
        </div>
        <div className="hidden animate-fade-up lg:block" style={{ animationDelay: '450ms' }}>
          <div className="animate-float">
            <QuickEmiCard />
          </div>
        </div>
      </div>
    </Section>
  );
}
