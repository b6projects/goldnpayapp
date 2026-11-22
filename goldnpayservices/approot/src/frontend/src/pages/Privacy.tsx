import LegalPage from '../components/legal/LegalPage';
import { site } from '../config/site';
import Seo from '../components/seo/Seo';
import { pageSeo } from '../seo/pageSeo';

export default function Privacy() {
  const g = site.grievanceOfficer;
  return (
    <LegalPage title="Privacy Policy">
      <Seo {...pageSeo('/privacy')} />
      <section aria-labelledby="pp-data" className="space-y-2">
        <h2 id="pp-data" className="text-xl font-semibold">Data we collect</h2>
        <p>When you ask for a consultation we collect your name, phone number, city and loan details.</p>
      </section>
      <section aria-labelledby="pp-purpose" className="space-y-2">
        <h2 id="pp-purpose" className="text-xl font-semibold">Why we use it</h2>
        <p>
          We use your details only to respond to your request and to help you with your loan enquiry. We do not
          sell your data.
        </p>
      </section>
      <section aria-labelledby="pp-sharing" className="space-y-2">
        <h2 id="pp-sharing" className="text-xl font-semibold">Sharing with partner lenders</h2>
        <p>We share your details with partner lenders only with your consent.</p>
      </section>
      <section aria-labelledby="pp-retention" className="space-y-2">
        <h2 id="pp-retention" className="text-xl font-semibold">Retention</h2>
        <p>We keep your details only as long as needed for the purpose above or as required by law.</p>
      </section>
      <section aria-labelledby="pp-rights" className="space-y-2">
        <h2 id="pp-rights" className="text-xl font-semibold">Your rights under the DPDP Act 2023</h2>
        <p>
          Under the Digital Personal Data Protection Act, 2023 you may withdraw your consent at any time, ask to
          access or correct your data, and request deletion of your data. To do so, contact the Grievance Officer
          below.
        </p>
      </section>
      <section aria-labelledby="pp-cookies" className="space-y-2">
        <h2 id="pp-cookies" className="text-xl font-semibold">Cookies and trackers</h2>
        <p>This website uses no cookies or trackers.</p>
      </section>
      <section aria-labelledby="pp-grievance" className="space-y-2">
        <h2 id="pp-grievance" className="text-xl font-semibold">Grievance Officer</h2>
        <p>Name: {g.name}</p>
        <p>
          Email:{' '}
          <a className="underline" href={`mailto:${g.email}`}>
            {g.email}
          </a>
        </p>
        <p>
          Phone:{' '}
          <a className="underline" href={`tel:${g.phone.replace(/\s/g, '')}`}>
            {g.phone}
          </a>
        </p>
      </section>
    </LegalPage>
  );
}
