import Section from '../components/ui/Section';
import EmiCalculator from '../components/tools/EmiCalculator';
import { getLoanBySlug } from '../data/loans';

export default function EmiCalculatorPage() {
  return (
    <Section>
      <h1 className="font-heading text-3xl font-semibold text-primary">EMI Calculator</h1>
      <p className="mt-2 mb-8 max-w-2xl text-muted">
        Estimate your monthly instalment for any loan type. All figures are indicative and the final
        rate and EMI are decided by the lender.
      </p>
      <EmiCalculator loan={getLoanBySlug('personal-loan')} allowLoanSwitch />
    </Section>
  );
}
