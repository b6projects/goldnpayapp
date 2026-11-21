import Section from '../ui/Section';
import Reveal from '../ui/Reveal';
import { howItWorksSteps } from '../../data/home';

export default function HowItWorks() {
  return (
    <Section aria-labelledby="how-heading">
      <h2 id="how-heading" className="heading-accent mb-8 text-2xl font-bold text-text md:text-3xl">
        How it works
      </h2>
      <ol className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {howItWorksSteps.map((step, i) => (
          <Reveal
            as="li"
            key={step.title}
            delay={i * 120}
            className="rounded-xl border border-surface bg-background p-6"
          >
            <span
              aria-hidden="true"
              className="mb-3 flex h-10 w-10 items-center justify-center rounded-full bg-primary font-bold text-white ring-4 ring-sand"
            >
              {i + 1}
            </span>
            <h3 className="text-lg font-semibold text-text">{step.title}</h3>
            <p className="mt-1 text-base text-muted">{step.description}</p>
          </Reveal>
        ))}
      </ol>
    </Section>
  );
}
