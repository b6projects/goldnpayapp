import { useParams } from 'react-router-dom';
import Section from '../components/ui/Section';
import Breadcrumb from '../components/ui/Breadcrumb';
import LoanHeader from '../components/loans/LoanHeader';
import LoanContentSections from '../components/loans/LoanContentSections';
import RelatedLoans from '../components/loans/RelatedLoans';
import ConsultationForm from '../components/forms/ConsultationForm';
import NotFound from './NotFound';
import { getLoanBySlug } from '../data/loans';
import Seo from '../components/seo/Seo';
import { loanMeta } from '../seo/meta';
import { breadcrumbSchema, faqSchema, loanProductSchema } from '../seo/schema';

export default function LoanDetail() {
  const { slug = '' } = useParams();
  const loan = getLoanBySlug(slug);
  if (!loan) return <NotFound />;

  return (
    <Section>
      <Seo
        {...loanMeta(loan)}
        path={`/loans/${loan.slug}`}
        jsonLd={[
          breadcrumbSchema([
            { name: 'Home', path: '/' },
            { name: 'Loans', path: '/loans' },
            { name: loan.title, path: `/loans/${loan.slug}` },
          ]),
          loanProductSchema(loan),
          faqSchema(loan.faqs),
        ]}
      />
      <div className="space-y-6">
        <Breadcrumb
          items={[{ label: 'Home', to: '/' }, { label: 'Loans', to: '/loans' }, { label: loan.title }]}
        />
        <LoanHeader loan={loan} />
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-[1fr_380px]">
          <div className="min-w-0 space-y-10">
            <LoanContentSections loan={loan} />
            <RelatedLoans loan={loan} />
          </div>
          <div data-testid="consultation-column" className="lg:sticky lg:top-24 lg:self-start">
            <ConsultationForm defaultLoan={loan.slug} lockLoan />
          </div>
        </div>
      </div>
    </Section>
  );
}
