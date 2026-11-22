import LegalPage from '../components/legal/LegalPage';
import Seo from '../components/seo/Seo';
import { pageSeo } from '../seo/pageSeo';

export default function Terms() {
  return (
    <LegalPage title="Terms of Use">
      <Seo {...pageSeo('/terms')} />
      <section aria-labelledby="t-service" className="space-y-2">
        <h2 id="t-service" className="text-xl font-semibold">Nature of our service</h2>
        <p>
          We are a loan consultant (DSA) and not a lender. We help you compare options and prepare applications.
        </p>
      </section>
      <section aria-labelledby="t-approval" className="space-y-2">
        <h2 id="t-approval" className="text-xl font-semibold">No guarantee</h2>
        <p>
          We do not guarantee approval, rates or terms. The final decision on any loan rests solely with the lender.
        </p>
      </section>
      <section aria-labelledby="t-rates" className="space-y-2">
        <h2 id="t-rates" className="text-xl font-semibold">Indicative information</h2>
        <p>All rates, amounts, EMI and eligibility results shown on this website are indicative only.</p>
      </section>
      <section aria-labelledby="t-user" className="space-y-2">
        <h2 id="t-user" className="text-xl font-semibold">Your responsibilities</h2>
        <p>You must give accurate and complete information when you contact us.</p>
      </section>
      <section aria-labelledby="t-third" className="space-y-2">
        <h2 id="t-third" className="text-xl font-semibold">Third-party lender terms</h2>
        <p>Each lender&apos;s own terms and conditions apply to any loan you take from them.</p>
      </section>
      <section aria-labelledby="t-liability" className="space-y-2">
        <h2 id="t-liability" className="text-xl font-semibold">Limitation of liability</h2>
        <p>
          To the extent permitted by law, we are not liable for decisions made by lenders or losses arising from use
          of this website.
        </p>
      </section>
      <section aria-labelledby="t-law" className="space-y-2">
        <h2 id="t-law" className="text-xl font-semibold">Governing law</h2>
        <p>These terms are governed by the laws of India.</p>
      </section>
    </LegalPage>
  );
}
