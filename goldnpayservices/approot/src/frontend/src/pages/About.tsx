import Section from '../components/ui/Section';
import Card from '../components/ui/Card';
import LinkButton from '../components/ui/LinkButton';

const values = [
  { title: 'Transparency', text: 'Clear, plain information about options, costs and who decides.' },
  { title: 'Customer first', text: 'We focus on what suits your situation, not on a single lender.' },
  { title: 'Responsible guidance', text: 'We encourage you to borrow only what you can comfortably repay.' },
];

export default function About() {
  return (
    <Section>
      <h1 className="text-3xl font-bold text-text md:text-4xl">About Us</h1>
      <section aria-labelledby="about-story" className="mt-6 max-w-3xl space-y-2">
        <h2 id="about-story" className="text-2xl font-bold text-text">Our story</h2>
        <p className="text-base text-text">TODO(owner): add the founder story and background.</p>
      </section>
      <section aria-labelledby="about-mission" className="mt-8 max-w-3xl space-y-2">
        <h2 id="about-mission" className="text-2xl font-bold text-text">Our mission</h2>
        <p className="text-base text-text">
          To help people understand their loan options and find the right fit, with honest guidance at every step.
        </p>
      </section>
      <section aria-labelledby="about-values" className="mt-8">
        <h2 id="about-values" className="mb-4 text-2xl font-bold text-text">Our values</h2>
        <ul className="grid gap-4 md:grid-cols-3">
          {values.map((v) => (
            <li key={v.title}>
              <Card className="h-full">
                <h3 className="text-lg font-semibold text-text">{v.title}</h3>
                <p className="mt-2 text-base text-text">{v.text}</p>
              </Card>
            </li>
          ))}
        </ul>
      </section>
      <section aria-labelledby="about-paid" className="mt-8 max-w-3xl space-y-2">
        <h2 id="about-paid" className="text-2xl font-bold text-text">How we&apos;re paid</h2>
        <p className="text-base text-text">TODO(owner): explain the fee policy and how the business is paid.</p>
      </section>
      <div className="mt-8">
        <LinkButton to="/contact">Contact us</LinkButton>
      </div>
    </Section>
  );
}
