import Section from '../ui/Section';
import Accordion from '../ui/Accordion';
import LinkButton from '../ui/LinkButton';
import { faqs } from '../../data/faqs';

export default function FaqPreview() {
  const items = faqs.slice(0, 5).map((f, i) => ({ id: `faq-${i}`, title: f.q, content: f.a }));
  return (
    <Section aria-labelledby="faq-heading">
      <h2 id="faq-heading" className="mb-6 text-2xl font-bold text-text md:text-3xl">
        Frequently asked questions
      </h2>
      <Accordion items={items} />
      <div className="mt-6">
        <LinkButton to="/faq" variant="secondary">
          View all FAQs
        </LinkButton>
      </div>
    </Section>
  );
}
