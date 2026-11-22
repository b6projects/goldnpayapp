import Section from '../components/ui/Section';
import EmiCalculator from '../components/tools/EmiCalculator';
import { getLoanBySlug } from '../data/loans';
import Seo from '../components/seo/Seo';
import { pageSeo } from '../seo/pageSeo';

export default function EmiCalculatorPage() {
  return (
    <Section className="py-4 md:py-6">
      <Seo {...pageSeo('/emi-calculator')} />
      <h1 className="font-heading text-2xl font-semibold text-primary">EMI Calculator</h1>
      <p className="mb-3 mt-1 max-w-3xl text-sm text-muted">
        Estimate your monthly instalment for any loan type. All figures are indicative and the final
        rate and EMI are decided by the lender.
      </p>
      <EmiCalculator loan={getLoanBySlug('personal-loan')} allowLoanSwitch />
    </Section>
  );
}
