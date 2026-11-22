import Section from '../components/ui/Section';
import Card from '../components/ui/Card';
import LinkButton from '../components/ui/LinkButton';
import { loans } from '../data/loans';
import Seo from '../components/seo/Seo';
import { pageSeo } from '../seo/pageSeo';

const services = [
  {
    title: 'Understand your need',
    text: 'We listen to why you need the money, how much, and what you can comfortably repay each month.',
  },
  {
    title: 'Match you with lenders',
    text: 'We know the products, rates and eligibility rules of our partner banks and NBFCs, and shortlist the ones that fit your profile.',
  },
  {
    title: 'Prepare your documents',
    text: 'We tell you exactly which documents are needed, check them for gaps, and help you avoid delays and rejections caused by missing papers.',
  },
  {
    title: 'Submit and follow up',
    text: "We submit your application to the lender, track it, answer the lender's queries and keep you updated until a decision is made.",
  },
  {
    title: 'Support through disbursal',
    text: 'We guide you through verification, sanction terms and disbursal, and explain what you are signing before you sign it.',
  },
  {
    title: 'Help with existing loans',
    text: 'We can review a loan you already have and tell you whether a balance transfer could lower your cost.',
  },
];

const steps = [
  'You share your requirement through a free consultation, a call or WhatsApp.',
  'We assess your income, existing obligations and credit profile to estimate what you are eligible for.',
  'We recommend suitable lenders and loan types, and explain the indicative rates, charges and tenure.',
  'You choose, and we help you gather and verify documents.',
  'We submit the application and coordinate with the lender.',
  'The lender decides, sanctions and disburses the loan. We stay in touch until it is complete.',
];

const values = [
  { title: 'Transparency', text: 'Clear, plain information about options, costs and who decides.' },
  { title: 'Customer first', text: 'We focus on what suits your situation, not on a single lender.' },
  { title: 'Responsible guidance', text: 'We encourage you to borrow only what you can comfortably repay.' },
];

export default function About() {
  return (
    <Section>
      <Seo {...pageSeo('/about')} />
      <h1 className="text-3xl font-bold text-text md:text-4xl">About Us</h1>

      <section aria-labelledby="about-who" className="mt-6 max-w-3xl space-y-3">
        <h2 id="about-who" className="text-2xl font-bold text-text">Who we are</h2>
        <p className="text-base text-text">
          GoldNPay is a loan consultancy based in Chennai. We work as a Direct Selling Agent
          (DSA): an independent intermediary that connects people who need a loan with the banks and non-banking
          financial companies (NBFCs) that provide one.
        </p>
        <p className="text-base text-text">
          We are not a lender. We do not lend our own money, and we do not decide whether a loan is approved. Our
          job is to make borrowing easier: to help you pick the right product, present a complete and accurate
          application, and guide you from the first enquiry to disbursal.
        </p>
      </section>

      <section aria-labelledby="about-dsa" className="mt-8 max-w-3xl space-y-3">
        <h2 id="about-dsa" className="text-2xl font-bold text-text">What is a loan DSA?</h2>
        <p className="text-base text-text">
          Banks and NBFCs appoint Direct Selling Agents to reach customers and help them apply. Each lender has its
          own products, interest rates, eligibility rules and document requirements, and these change often.
          Visiting each lender separately takes time, and it is hard to know which one suits you best.
        </p>
        <p className="text-base text-text">
          A DSA works across several lenders. That lets us compare options for you in one place, tell you honestly
          where your profile is strong or weak, and help you apply to the lender most likely to suit you. The
          lender always makes the final decision on approval, the rate and the terms.
        </p>
      </section>

      <section aria-labelledby="about-services" className="mt-8">
        <h2 id="about-services" className="mb-4 text-2xl font-bold text-text">What we do for you</h2>
        <ul className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {services.map((s) => (
            <li key={s.title}>
              <Card className="h-full">
                <h3 className="text-lg font-semibold text-text">{s.title}</h3>
                <p className="mt-2 text-base text-text">{s.text}</p>
              </Card>
            </li>
          ))}
        </ul>
      </section>

      <section aria-labelledby="about-process" className="mt-8 max-w-3xl space-y-3">
        <h2 id="about-process" className="text-2xl font-bold text-text">How it works</h2>
        <ol className="list-decimal space-y-2 pl-6 text-base text-text">
          {steps.map((step) => (
            <li key={step}>{step}</li>
          ))}
        </ol>
      </section>

      <section aria-labelledby="about-loans" className="mt-8 max-w-3xl space-y-3">
        <h2 id="about-loans" className="text-2xl font-bold text-text">Loans we help with</h2>
        <p className="text-base text-text">
          We help salaried professionals, self-employed people and business owners with:
        </p>
        <ul className="list-disc space-y-1 pl-6 text-base text-text">
          {loans.map((l) => (
            <li key={l.slug}>{l.title}</li>
          ))}
        </ul>
      </section>

      <section aria-labelledby="about-limits" className="mt-8 max-w-3xl space-y-3">
        <h2 id="about-limits" className="text-2xl font-bold text-text">What you should know</h2>
        <ul className="list-disc space-y-2 pl-6 text-base text-text">
          <li>We are an intermediary, not a lender. Approval, interest rate, fees and terms are set by the lender.</li>
          <li>We cannot guarantee approval or a particular rate. Rates and amounts shown on this site are indicative.</li>
          <li>Please read the lender's sanction letter and loan agreement carefully before you accept a loan.</li>
          <li>Borrow only what you can repay comfortably. Missed payments can affect your credit score.</li>
        </ul>
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

      <div className="mt-8 flex flex-wrap gap-3">
        <LinkButton to="/consultation">Get a free consultation</LinkButton>
        <LinkButton to="/contact" variant="secondary">Contact us</LinkButton>
      </div>
    </Section>
  );
}
