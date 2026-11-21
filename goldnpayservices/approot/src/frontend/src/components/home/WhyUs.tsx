import Section from '../ui/Section';
import Card from '../ui/Card';
import Reveal from '../ui/Reveal';
import { whyUsPoints } from '../../data/home';

export default function WhyUs() {
  return (
    <Section className="bg-surface" aria-labelledby="why-heading">
      <h2 id="why-heading" className="heading-accent mb-8 text-2xl font-bold text-text md:text-3xl">
        Why choose us
      </h2>
      <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {whyUsPoints.map((p, i) => (
          <Reveal as="li" key={p.title} delay={i * 100}>
            <Card className="h-full">
              <h3 className="text-lg font-semibold text-text">{p.title}</h3>
              <p className="mt-1 text-base text-muted">{p.description}</p>
            </Card>
          </Reveal>
        ))}
      </ul>
    </Section>
  );
}
