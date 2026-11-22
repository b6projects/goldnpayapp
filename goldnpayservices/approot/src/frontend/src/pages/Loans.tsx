import Section from '../components/ui/Section';
import LoanGrid from '../components/home/LoanGrid';
import Seo from '../components/seo/Seo';
import { pageSeo } from '../seo/pageSeo';

export default function Loans() {
  return (
    <>
      <Seo {...pageSeo('/loans')} />
      <Section className="pb-0 md:pb-0">
        <h1 className="text-3xl font-bold text-text md:text-4xl">Loan Types</h1>
        {/* TODO(owner): replace with approved intro copy */}
        <p className="mt-3 max-w-3xl text-lg text-muted">
          Compare the loan types we help you explore, from personal and home loans to business and gold
          loans. Choose one to see its features, eligibility and documents, then request a free
          consultation.
        </p>
      </Section>
      <LoanGrid />
    </>
  );
}
