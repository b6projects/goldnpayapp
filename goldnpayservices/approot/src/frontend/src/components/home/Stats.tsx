import Section from '../ui/Section';
import Reveal from '../ui/Reveal';
import CountUp from '../ui/CountUp';
import { stats } from '../../data/stats';

export default function Stats() {
  return (
    <Section className="bg-primary text-white" aria-labelledby="stats-heading">
      <h2 id="stats-heading" className="sr-only">
        Our numbers
      </h2>
      <dl className="grid grid-cols-2 gap-6 text-center lg:grid-cols-4">
        {stats.map((s, i) => (
          <Reveal key={s.label} delay={i * 120} className="rounded-xl border border-white/10 py-4">
            <dd className="text-4xl font-bold text-sand">
              <CountUp value={s.value} />
            </dd>
            <dt className="mt-1 text-sm">{s.label}</dt>
          </Reveal>
        ))}
      </dl>
    </Section>
  );
}
