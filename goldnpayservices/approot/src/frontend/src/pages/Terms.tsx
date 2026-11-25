import { Link } from 'react-router-dom';
import LegalPage from '../components/legal/LegalPage';
import { Bullets, LastUpdated, LegalSection, Mail, Numbered, Tel } from '../components/legal/LegalSection';
import Seo from '../components/seo/Seo';
import { site } from '../config/site';
import { pageSeo } from '../seo/pageSeo';

export default function Terms() {
  const l = site.legal;
  const g = site.grievanceOfficer;
  return (
    <LegalPage title="Terms of Use">
      <Seo {...pageSeo('/terms')} />
      <LastUpdated date={l.lastUpdated} />
      <p>
        By using this website or any {site.name} service, you agree to these Terms, the{' '}
        <Link className="underline" to="/disclaimer">
          Disclaimer
        </Link>{' '}
        and the{' '}
        <Link className="underline" to="/privacy">
          Privacy Policy
        </Link>
        . If you do not agree, please do not use our services.
      </p>

      <LegalSection id="t-eligibility" title="1. Eligibility">
        <p>
          You must be at least 18 years old, a resident of India, and legally able to enter into a contract. For gold
          transactions, you must be the lawful owner of the gold or authorised in writing by the owner.
        </p>
      </LegalSection>

      <LegalSection id="t-loans" title="2. Loan facilitation services">
        <Numbered
          items={[
            'We act as a DSA for our partner lenders and are not a lender. We help you choose a product, collect your documents and submit your application.',
            'You authorise us to share your application and documents with the lender(s) you agree to, and to follow up on your behalf.',
            'The lender alone decides whether to approve your loan and on what terms. Rates, EMIs and eligibility we show are indicative only. We are not liable for a rejection, delay or change in terms.',
            "You must read and accept the lender's loan agreement, sanction letter and Key Facts Statement before you sign. Repayment obligations are owed to the lender, not to " +
              site.name +
              '.',
            ['We are paid a commission by the lender.', l.loanFeeStatement].filter(Boolean).join(' '),
            "We do not collect EMIs or loan repayments. Pay only into the lender's official accounts.",
          ]}
        />
      </LegalSection>

      <LegalSection id="t-gold" title="3. Old gold purchase">
        <Numbered
          items={[
            'You declare that the gold you sell is your own, lawfully acquired, and free of any pledge, lien or dispute.',
            'You must provide valid KYC (PAN, and Aadhaar or another officially valid document). We may refuse a transaction if KYC is incomplete or we have reason to doubt ownership.',
            'Gold is tested for purity and weighed in your presence. Melting is done only with your written consent.',
            'The price is the day\'s rate for the tested purity and net weight, less any deductions disclosed to you in writing before you agree. Our quote is valid only at the time it is given.',
            `Once you accept the price and sign the purchase invoice, the sale is final and ownership passes to ${site.name}.`,
            'Payment is made by bank transfer (IMPS/NEFT/RTGS) or UPI to an account in your own name. Cash is paid only within the limits allowed by law.',
            'We must report suspicious transactions to the authorities as required by law, and may hold gold we suspect is stolen.',
          ]}
        />
      </LegalSection>

      <LegalSection id="t-release" title="4. Pledged gold release">
        <Numbered
          items={[
            `You authorise ${site.name} to pay your existing gold loan lender the amount needed to close your loan and release your pledged gold.`,
            "You must give us correct loan details and the pledge receipt, and accompany our staff to the lender's branch to collect the gold.",
            `After release, the gold is tested and valued as in section 3. If you choose to sell it to us, we deduct the amount we paid to your lender and our service charge of ${l.pledgedReleaseServiceCharge} from the purchase price and pay you the balance.`,
            <>
              If you decide <strong>not</strong> to sell after we have released your gold, you must repay us the full
              amount paid to your lender, plus a flat transaction fee of {l.pledgedReleaseWithdrawalFee}, before the
              gold is handed back to you.
            </>,
            'If the released gold is worth less than the amount we paid to your lender, you must pay us the difference.',
            'You are responsible for any amount owed to your lender that you did not disclose to us.',
          ]}
        />
      </LegalSection>

      <LegalSection id="t-user" title="5. Your responsibilities">
        <Bullets
          items={[
            'Give true, complete and current information and genuine documents.',
            'Do not use our services for any unlawful purpose, including money laundering or the sale of stolen property.',
            'Do not misuse this website, try to gain unauthorised access, or copy its content for commercial use.',
            'Keep us informed of changes to your contact details.',
          ]}
        />
        <p>Giving false information or forged documents may lead to refusal of service and a report to the authorities.</p>
      </LegalSection>

      <LegalSection id="t-comms" title="6. Communication">
        <p>
          You agree that {site.name} and our partner lenders may contact you about your enquiry or application by
          phone, SMS, WhatsApp or email, even if your number is on the DND register. Marketing messages are sent only
          with your consent, and you can opt out at any time.
        </p>
      </LegalSection>

      <LegalSection id="t-ip" title="7. Intellectual property">
        <p>
          The {site.name} name, logo and website content belong to {site.name}. Lender names and logos belong to their
          owners. You may not copy or reuse them without written permission.
        </p>
      </LegalSection>

      <LegalSection id="t-liability" title="8. Limitation of liability">
        <p>
          To the extent permitted by law, {site.name} is not liable for indirect or consequential losses, or for any
          loss caused by a lender&apos;s decision or by events outside our reasonable control.
        </p>
      </LegalSection>

      <LegalSection id="t-indemnity" title="9. Indemnity">
        <p>
          You agree to compensate {site.name} for any loss we suffer because you gave false information, used forged
          documents, or sold or pledged gold you did not own.
        </p>
      </LegalSection>

      <LegalSection id="t-suspension" title="10. Suspension">
        <p>
          We may refuse or stop any service if we reasonably believe information is false, a transaction is unlawful,
          or these Terms have been breached.
        </p>
      </LegalSection>

      <LegalSection id="t-changes" title="11. Changes to these Terms">
        <p>
          We may update these Terms. The date at the top shows the latest version. Transactions already agreed are
          governed by the Terms in force when they were agreed.
        </p>
      </LegalSection>

      <LegalSection id="t-law" title="12. Governing law and disputes">
        <p>
          These Terms are governed by the laws of India. We will first try to resolve any dispute through our
          grievance process. If it cannot be resolved, the courts in <strong>Chennai, Tamil Nadu</strong> will have
          exclusive jurisdiction.
        </p>
      </LegalSection>

      <LegalSection id="t-contact" title="13. Contact">
        <p>
          {l.entityName}, {site.address}, Tamil Nadu. Phone: <Tel phone={g.phone} /> ({g.hours}). Email:{' '}
          <Mail email={l.supportEmail} />. Grievance Officer: {g.name}, <Mail email={g.email} />.
        </p>
      </LegalSection>
    </LegalPage>
  );
}
