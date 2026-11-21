import Section from '../ui/Section';
import LinkButton from '../ui/LinkButton';

export default function CtaBand() {
  return (
    <Section className="bg-secondary text-white" aria-labelledby="cta-heading">
      <div className="flex flex-col items-start gap-4 md:flex-row md:items-center md:justify-between">
        <h2 id="cta-heading" className="text-2xl font-bold">
          Not sure which loan is right for you?
        </h2>
        <LinkButton to="/consultation" variant="secondary" size="lg">
          Get Free Consultation
        </LinkButton>
      </div>
    </Section>
  );
}
