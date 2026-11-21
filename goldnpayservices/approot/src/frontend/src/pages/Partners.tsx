import Section from '../components/ui/Section';
import Disclaimer from '../components/ui/Disclaimer';
import BankBadge from '../components/ui/BankBadge';
import { partners } from '../data/partners';

export default function Partners() {
  return (
    <Section>
      <h1 className="text-3xl font-bold text-text md:text-4xl">Partner Disclosure</h1>
      <p className="mt-3 max-w-3xl text-lg text-muted">
        We work with the following regulated entities. The final decision on any loan rests solely with the lender.
      </p>
      <div className="mt-6 overflow-x-auto">
        <table className="w-full max-w-3xl border-collapse text-left text-base text-text">
          <caption className="sr-only">Partner lenders</caption>
          <thead>
            <tr className="border-b border-surface">
              <th scope="col" className="py-2 pr-4">Name</th>
              <th scope="col" className="py-2">Type</th>
            </tr>
          </thead>
          <tbody>
            {partners.map((p) => (
              <tr key={p.name} className="border-b border-surface">
                <td className="py-2 pr-4">
                  <BankBadge partner={p} />
                </td>
                <td className="py-2">
                  {p.sector} sector {p.type.toLowerCase()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="mt-6">
        <Disclaimer>
          This is an indicative list of banks. Names are shown as text for identification only, no logos are used, and all names and trademarks belong to their respective owners. Listing does not imply endorsement.
        </Disclaimer>
      </div>
    </Section>
  );
}
