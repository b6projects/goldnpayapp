import LegalPage from '../components/legal/LegalPage';
import Seo from '../components/seo/Seo';
import { pageSeo } from '../seo/pageSeo';

export default function Disclaimer() {
  return (
    <LegalPage title="Disclaimer">
      <Seo {...pageSeo('/disclaimer')} />
      <p>We are not a lender. We are a loan consultant (DSA) who helps you explore loan options.</p>
      <p>
        All rates, amounts, EMI and eligibility results on this website are indicative and are not an offer. The
        final decision on any loan rests solely with the lender, and we do not guarantee approval.
      </p>
      <p>Some content on this website, such as testimonials and partner names, is sample content.</p>
      <p>Nothing on this website is financial or legal advice.</p>
    </LegalPage>
  );
}
