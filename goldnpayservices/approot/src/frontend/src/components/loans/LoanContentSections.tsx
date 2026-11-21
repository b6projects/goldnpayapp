import type { ReactNode } from 'react';
import Accordion from '../ui/Accordion';
import Tabs from '../ui/Tabs';
import EmiCalculator from '../tools/EmiCalculator';
import type { Loan } from '../../types';

function List({ items }: { items: string[] }) {
  return (
    <ul className="list-disc space-y-1 pl-5 text-base text-text">
      {items.map((item) => (
        <li key={item}>{item}</li>
      ))}
    </ul>
  );
}

function Block({ id, title, children }: { id: string; title: string; children: ReactNode }) {
  return (
    <section aria-labelledby={id} className="space-y-3">
      <h2 id={id} className="text-2xl font-bold text-text">
        {title}
      </h2>
      {children}
    </section>
  );
}

export default function LoanContentSections({ loan }: { loan: Loan }) {
  return (
    <div className="space-y-10">
      <Block id="ideal-for-heading" title="Ideal for">
        <List items={loan.idealFor} />
      </Block>
      <Block id="features-heading" title="Key features">
        <List items={loan.features} />
      </Block>
      <Block id="eligibility-heading" title="Eligibility">
        <List items={loan.eligibility} />
      </Block>
      <Block id="documents-heading" title="Documents required">
        <Tabs
          label="Documents by applicant type"
          tabs={[
            { id: 'salaried', label: 'Salaried', content: <List items={loan.documents.salaried} /> },
            {
              id: 'self-employed',
              label: 'Self-employed',
              content: <List items={loan.documents.selfEmployed} />,
            },
          ]}
        />
      </Block>
      <EmiCalculator key={loan.slug} loan={loan} />
      <Block id="faqs-heading" title="Frequently asked questions">
        <Accordion
          items={loan.faqs.map((f, i) => ({
            id: `${loan.slug}-faq-${i}`,
            title: f.q,
            content: f.a,
          }))}
        />
      </Block>
    </div>
  );
}
