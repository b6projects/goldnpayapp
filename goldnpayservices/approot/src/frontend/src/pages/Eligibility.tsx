import Section from '../components/ui/Section';
import EligibilityChecker from '../components/tools/EligibilityChecker';

export default function Eligibility() {
  return (
    <Section>
      <h1 className="font-heading text-3xl font-semibold text-primary">
        Loan Eligibility Calculator
      </h1>
      <p className="mt-2 mb-8 max-w-2xl text-muted">
        Get an indicative idea of how much you may be able to borrow based on your income and
        existing EMIs. The result is an estimate, not an offer.
      </p>
      <EligibilityChecker />
    </Section>
  );
}
