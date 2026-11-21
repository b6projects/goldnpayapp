import Section from '../ui/Section';
import Reveal from '../ui/Reveal';
import LinkButton from '../ui/LinkButton';

export default function CtaBand() {
  return (
    <Section
      className="bg-gradient-to-r from-secondary via-primary to-secondary text-white"
      aria-labelledby="cta-heading"
    >
      <Reveal className="flex flex-col items-start gap-4 md:flex-row md:items-center md:justify-between">
        <h2 id="cta-heading" className="text-2xl font-bold">
          Not sure which loan is right for you?
        </h2>
        <LinkButton to="/consultation" variant="secondary" size="lg" className="animate-float">
          Get Free Consultation
        </LinkButton>
      </Reveal>
    </Section>
  );
}
