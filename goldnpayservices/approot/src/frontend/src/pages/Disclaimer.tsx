import { Link } from 'react-router-dom';
import LegalPage from '../components/legal/LegalPage';
import { Bullets, LastUpdated, LegalSection, Mail, Numbered, Tel } from '../components/legal/LegalSection';
import Seo from '../components/seo/Seo';
import { site } from '../config/site';
import { pageSeo } from '../seo/pageSeo';

export default function Disclaimer() {
  const l = site.legal;
  const g = site.grievanceOfficer;
  const [phone1, phone2] = l.officialPhones;
  return (
    <LegalPage title="Disclaimer">
      <Seo {...pageSeo('/disclaimer')} />
      <LastUpdated date={l.lastUpdated} />
      <p>
        {site.name} is a loan facilitator and gold buyer. We are not a bank, an NBFC or a lender, and we do not lend
        money from our own funds.
      </p>

      <LegalSection id="d-who" title="1. Who we are">
        <p>
          This website, www.goldnpay.in, is operated by {l.entityName} (&quot;{site.name}&quot;, &quot;we&quot;,
          &quot;us&quot;, &quot;our&quot;), {l.entityType}, with its registered office at {site.address}, Tamil Nadu.
          GSTIN: {l.gstin}.
        </p>
        <p>We offer three services:</p>
        <Numbered
          items={[
            <>
              <strong>Loan facilitation</strong> as a Direct Selling Agent (DSA) for our partner banks and NBFCs.
            </>,
            <>
              <strong>Old gold purchase</strong>, where we buy gold jewellery and ornaments from you.
            </>,
            <>
              <strong>Pledged gold release</strong>, where we help you release gold pledged with a bank or NBFC and,
              if you choose, buy it from you.
            </>,
          ]}
        />
      </LegalSection>

      <LegalSection id="d-dsa" title="2. We are a DSA, not a lender">
        <Bullets
          items={[
            'We collect your application and documents and pass them to our partner lenders. We may help you compare offers and follow up on your application.',
            <>
              Every decision on approval, loan amount, interest rate, fees, tenure and disbursement is made{' '}
              <strong>only by the lender</strong>, under its own credit policy. We cannot promise or guarantee that
              any loan will be approved.
            </>,
            "Your loan agreement is between you and the lender. The lender's terms, privacy policy and Key Facts Statement (KFS) govern the loan. Please read them before you sign.",
            "Interest rates, EMIs and eligibility shown on this website or by our staff are indicative only. The figures in the lender's sanction letter and KFS are the ones that apply.",
            <>
              Our partner lenders are listed on our{' '}
              <Link className="underline" to="/partners">
                Partners page
              </Link>
              . The list is updated when partnerships change. You can ask us at any time which lender your
              application has been sent to.
            </>,
          ]}
        />
      </LegalSection>

      <LegalSection id="d-fraud" title="3. Beware of fraud">
        <Bullets
          items={[
            ...(l.loanFeeStatement ? [<strong key="fee">{l.loanFeeStatement}</strong>] : []),
            'We will never ask you to pay money into a personal bank account or UPI ID, or to pay an "advance" or "insurance fee" to get a loan approved.',
            'We will never ask for your OTP, card PIN, CVV or net-banking password.',
            <>
              Our staff carry a {site.name} ID card and an authorisation letter. Calls and messages come only from{' '}
              <Tel phone={phone1} /> and <Tel phone={phone2} />, and from @{l.emailDomain} email addresses.
            </>,
            <>
              If anyone asks you for money in {site.name}&apos;s name, do not pay. Report it to us at{' '}
              <Tel phone={phone1} /> or <Mail email={l.supportEmail} />, to the National Cyber Crime Helpline{' '}
              <strong>1930</strong>, or at{' '}
              <a className="underline" href="https://cybercrime.gov.in" target="_blank" rel="noopener noreferrer">
                cybercrime.gov.in
              </a>
              .
            </>,
          ]}
        />
      </LegalSection>

      <LegalSection id="d-gold" title="4. Gold purchase and valuation">
        <Bullets
          items={[
            'Gold prices quoted on this website, by phone or on WhatsApp are indicative and change through the day. The final price is set only after we test the purity and weight of your gold in your presence, at the rate in force at that time.',
            'Purity testing methods (such as XRF/karat meter testing and, with your written consent, melting) may give slightly different results from the purity stated on a bill or hallmark.',
            'Stones, beads, lac, wax and other non-gold parts are not paid for.',
          ]}
        />
      </LegalSection>

      <LegalSection id="d-info" title="5. Information on this website">
        <Bullets
          items={[
            'Content on this website is for general information only. It is not financial, legal or tax advice.',
            'We try to keep information accurate and current but do not guarantee that it is complete or error-free.',
            'Calculators (EMI, eligibility) give estimates only.',
            'Links to lender or third-party websites are for convenience. We are not responsible for their content or privacy practices.',
            'Lender names belong to their owners. Showing them means only that we have a DSA arrangement with that lender, not that the lender endorses our other services.',
            ...(site.showSampleTestimonials
              ? ['Some content on this website, such as testimonials and partner names, is sample content.']
              : []),
          ]}
        />
      </LegalSection>

      <LegalSection id="d-liability" title="6. Limitation of liability">
        <p>
          To the extent permitted by law, {site.name} is not liable for any loss arising from a lender&apos;s
          decision, delay or terms, from your reliance on indicative figures, or from any interruption to this
          website.
        </p>
      </LegalSection>

      <LegalSection id="d-contact" title="7. Contact">
        <p>
          Grievance Officer: {g.name}, <Tel phone={g.phone} /> ({g.hours}), <Mail email={g.email} />, {site.address}.
          See the Grievance Officer section of our{' '}
          <Link className="underline" to="/privacy">
            Privacy Policy
          </Link>
          .
        </p>
      </LegalSection>
    </LegalPage>
  );
}
