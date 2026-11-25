import { Link } from 'react-router-dom';
import LegalPage from '../components/legal/LegalPage';
import { Bullets, LastUpdated, LegalSection, Mail, Tel } from '../components/legal/LegalSection';
import Seo from '../components/seo/Seo';
import { site } from '../config/site';
import { pageSeo } from '../seo/pageSeo';

const dataTable: Array<[string, string, string]> = [
  ['Identity and contact', 'Name, date of birth, mobile number, email, address, photograph', 'Enquiry, application, gold transaction'],
  ['KYC documents', 'PAN, Aadhaar (masked where possible), voter ID, passport, driving licence', 'Loan application, gold purchase, pledged gold release'],
  ['Financial details', 'Income, employer, bank statements, salary slips, ITRs, existing loans', 'Loan application'],
  ['Credit information', 'Credit score and report from a credit bureau', 'Only with your explicit consent'],
  ['Bank details for payment', 'Account holder name, account number, IFSC, UPI ID', 'Gold purchase or release payouts'],
  ['Pledged gold loan details', 'Lender, loan account number, outstanding amount, pledge receipt', 'Pledged gold release'],
  ['Gold transaction records', 'Item description, weight, purity results, photographs, invoice', 'Gold purchase'],
  ['Website and device data', 'IP address, browser, pages visited, cookies', 'Website visits'],
];

export default function Privacy() {
  const l = site.legal;
  const g = site.grievanceOfficer;
  return (
    <LegalPage title="Privacy Policy">
      <Seo {...pageSeo('/privacy')} />
      <LastUpdated date={l.lastUpdated} />
      <p>
        We collect only the personal data needed to process your loan application or gold transaction, share it
        only with the lenders and service providers involved, and never sell it.
      </p>

      <LegalSection id="pp-scope" title="1. Scope and law">
        <p>
          This policy applies to personal data that {l.entityName} (&quot;{site.name}&quot;) collects through this
          website, our WhatsApp number ({site.phone}), phone calls, and visits by our staff or at our branch. It is
          written to comply with the Information Technology Act, 2000 and its rules, and the Digital Personal Data
          Protection Act, 2023 (DPDP Act 2023) and its rules as they come into force. For the purposes of the DPDP
          Act 2023, {site.name} is the <strong>Data Fiduciary</strong> for the data we collect.
        </p>
      </LegalSection>

      <LegalSection id="pp-data" title="2. What we collect">
        <div className="overflow-x-auto">
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr>
                <th scope="col" className="border-b border-muted p-2">Category</th>
                <th scope="col" className="border-b border-muted p-2">Examples</th>
                <th scope="col" className="border-b border-muted p-2">When</th>
              </tr>
            </thead>
            <tbody>
              {dataTable.map(([category, examples, when]) => (
                <tr key={category}>
                  <td className="border-b border-muted p-2 font-medium">{category}</td>
                  <td className="border-b border-muted p-2">{examples}</td>
                  <td className="border-b border-muted p-2">{when}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p>
          We do not collect your passwords, OTPs, card PINs or CVVs, and we do not access your phone&apos;s contacts,
          photos or messages. Please do not send KYC documents through the website contact form; we collect them
          securely when your application proceeds.
        </p>
      </LegalSection>

      <LegalSection id="pp-purpose" title="3. Why we use it">
        <Bullets
          items={[
            'To check eligibility and submit your loan application to partner lenders you agree to.',
            'To verify your identity and complete KYC, as required by law and by lenders.',
            'To value, buy and pay for gold, and to settle your pledged gold loan with your lender.',
            'To contact you about your application or transaction by phone, SMS, WhatsApp or email.',
            'To keep records required by tax, anti-money-laundering and other laws.',
            'To prevent fraud and to handle complaints and legal claims.',
            <>
              To send offers and updates, <strong>only if you opt in</strong>. You can opt out at any time.
            </>,
          ]}
        />
      </LegalSection>

      <LegalSection id="pp-consent" title="4. Consent">
        <p>
          We process your data on the basis of your consent, given through a clear consent form or checkbox, or where
          the law otherwise allows it (for example, to meet a legal obligation). Consent for a credit bureau check is
          asked separately. You can withdraw your consent at any time by writing to <Mail email={l.supportEmail} />;
          this does not affect processing already done, and we may be unable to continue a pending application.
        </p>
      </LegalSection>

      <LegalSection id="pp-sharing" title="5. Who we share it with">
        <p>We share your personal data only with your consent, or where the law requires it:</p>
        <Bullets
          items={[
            <>
              <strong>Partner lenders</strong> you choose or agree to, so that they can assess and process your loan.
              Their own privacy policies then apply.
            </>,
            <>
              <strong>Credit bureaus</strong> (such as CIBIL, Experian, Equifax, CRIF High Mark), only with your
              consent.
            </>,
            <>
              <strong>Your existing gold loan lender</strong>, to settle the loan and release your pledged gold.
            </>,
            <>
              <strong>Service providers</strong> that work for us under contract and confidentiality terms: KYC
              verification, cloud hosting, SMS/email/WhatsApp messaging, and payment processing.
            </>,
            <>
              <strong>Government and regulators</strong> when required by law, a court order or a lawful request.
            </>,
          ]}
        />
        <p>We do not sell, rent or trade your personal data.</p>
      </LegalSection>

      <LegalSection id="pp-security" title="6. Storage and security">
        <Bullets
          items={[
            'Your data is stored on servers located in India.',
            'We use encryption in transit (HTTPS), access controls limited to staff who need the data, and secure storage of physical documents.',
            'If a personal data breach occurs, we will inform you and the authorities as the law requires.',
          ]}
        />
      </LegalSection>

      <LegalSection id="pp-retention" title="7. How long we keep it">
        <Bullets
          items={[
            'Loan enquiries that do not proceed: up to 12 months, then deleted.',
            'Loan applications submitted to lenders: for the period required under our agreements with lenders and applicable law.',
            <>
              Gold purchase and release records, KYC and invoices: at least <strong>5 years</strong> after the
              transaction, or longer where tax or anti-money-laundering law requires.
            </>,
            'After this, data is deleted or anonymised.',
          ]}
        />
      </LegalSection>

      <LegalSection id="pp-rights" title="8. Your rights">
        <p>Under the DPDP Act 2023, you can ask us to:</p>
        <Bullets
          items={[
            'give you a summary of the personal data we hold and who we shared it with;',
            'correct, complete or update it;',
            'erase it, unless we must keep it by law (you can request deletion at any time);',
            'nominate a person to exercise these rights if you die or become unable to act.',
          ]}
        />
        <p>
          Write to <Mail email={l.supportEmail} />. We will reply within 90 days.
        </p>
      </LegalSection>

      <LegalSection id="pp-cookies" title="9. Cookies">
        <p>
          We use essential cookies to run the website. We also use Google Tag Manager to measure visits and
          understand which pages are useful; it and the analytics tags it loads may set cookies and send usage data,
          such as pages viewed and approximate location, to Google. We do not use these tools to build advertising
          profiles. You can block cookies in your browser; some features may then not work.
        </p>
      </LegalSection>

      <LegalSection id="pp-children" title="10. Children">
        <p>Our services are for people aged 18 and above. We do not knowingly collect data from children.</p>
      </LegalSection>

      <LegalSection id="pp-changes" title="11. Changes">
        <p>
          We may update this policy. The date at the top shows the latest version. Significant changes will be
          notified on the website or by message.
        </p>
      </LegalSection>

      <LegalSection id="pp-grievance" title="12. Grievance Officer">
        <p>If you have a question or complaint about your personal data:</p>
        <p>
          <strong>Grievance Officer:</strong> {g.name}
        </p>
        <p>
          <strong>Email:</strong> <Mail email={g.email} />
        </p>
        <p>
          <strong>Phone:</strong> <Tel phone={g.phone} />, {g.hours}
        </p>
        <p>
          <strong>Address:</strong> {site.address}, Tamil Nadu
        </p>
        <p>
          We will acknowledge your complaint within 48 hours and aim to resolve it within 30 days. If you are not
          satisfied, you may approach the Data Protection Board of India once it is operational. For complaints about
          a loan, you may also contact the lender&apos;s grievance officer or the RBI Integrated Ombudsman at{' '}
          <a className="underline" href="https://cms.rbi.org.in" target="_blank" rel="noopener noreferrer">
            cms.rbi.org.in
          </a>
          . See also our{' '}
          <Link className="underline" to="/disclaimer">
            Disclaimer
          </Link>
          .
        </p>
      </LegalSection>
    </LegalPage>
  );
}
