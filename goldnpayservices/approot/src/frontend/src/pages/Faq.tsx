import { useState } from 'react';
import Section from '../components/ui/Section';
import Accordion from '../components/ui/Accordion';
import Input from '../components/ui/Input';
import { faqs } from '../data/faqs';
import { filterFaqs, groupFaqsByCategory } from '../lib/faqSearch';

export default function Faq() {
  const [query, setQuery] = useState('');
  const results = filterFaqs(faqs, query);
  const groups = groupFaqsByCategory(results);
  const count = results.length;
  return (
    <Section>
      <h1 className="text-3xl font-bold text-text md:text-4xl">Frequently Asked Questions</h1>
      <div className="mt-6 max-w-xl">
        <Input
          label="Search questions"
          type="search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>
      <p role="status" aria-live="polite" className="mt-2 text-sm text-muted">
        {count === 0
          ? 'No questions match your search'
          : `${count} ${count === 1 ? 'question' : 'questions'} found`}
      </p>
      <div className="mt-6 space-y-8">
        {groups.map(([category, list]) => (
          <section key={category} aria-labelledby={`faq-cat-${category}`}>
            <h2 id={`faq-cat-${category}`} className="mb-3 text-2xl font-bold text-text">
              {category}
            </h2>
            <Accordion
              items={list.map((f, i) => ({ id: `faq-${category}-${i}`, title: f.q, content: f.a }))}
            />
          </section>
        ))}
      </div>
    </Section>
  );
}
