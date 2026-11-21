import Section from '../ui/Section';
import { stats } from '../../data/stats';

export default function Stats() {
  return (
    <Section className="bg-primary text-white" aria-labelledby="stats-heading">
      <h2 id="stats-heading" className="sr-only">
        Our numbers
      </h2>
      <dl className="grid grid-cols-2 gap-6 text-center lg:grid-cols-4">
        {stats.map((s) => (
          <div key={s.label}>
            <dd className="text-3xl font-bold">{s.value}</dd>
            <dt className="mt-1 text-sm">{s.label}</dt>
          </div>
        ))}
      </dl>
    </Section>
  );
}
