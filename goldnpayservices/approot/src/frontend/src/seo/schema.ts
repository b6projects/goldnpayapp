import { site } from '../config/site';
import type { Faq, Loan } from '../types';
import { BRAND, OG_IMAGE_PATH, SERVICE_AREA, SITE_URL, absoluteUrl } from './meta';

export type JsonLd = Record<string, unknown>;

const CONTEXT = 'https://schema.org';

const isPlaceholder = (text: string) => text.includes('TODO(owner)');

const areaServed = () => ({ '@type': 'State', name: site.serviceArea, containedInPlace: { '@type': 'Country', name: 'India' } });

/** Business details for the homepage: a FinancialService, which is also a LocalBusiness. */
export function organizationSchema(): JsonLd {
  const sameAs = Object.values(site.social).filter((u) => u !== '');
  return {
    '@context': CONTEXT,
    '@type': 'FinancialService',
    '@id': `${SITE_URL}/#organization`,
    name: site.name,
    alternateName: BRAND,
    description: `${site.name} is a loan consultant (not a lender) that helps you compare and apply for loans from partner banks and NBFCs.`,
    url: `${SITE_URL}/`,
    logo: absoluteUrl('/images/logo.png'),
    image: absoluteUrl(OG_IMAGE_PATH),
    telephone: site.phoneE164,
    address: {
      '@type': 'PostalAddress',
      streetAddress: site.addressDetail.streetAddress,
      addressLocality: site.addressDetail.locality,
      addressRegion: site.addressDetail.region,
      postalCode: site.addressDetail.postalCode,
      addressCountry: site.addressDetail.country,
    },
    areaServed: areaServed(),
    openingHoursSpecification: {
      '@type': 'OpeningHoursSpecification',
      dayOfWeek: site.openingHours.days,
      opens: site.openingHours.opens,
      closes: site.openingHours.closes,
    },
    ...(sameAs.length > 0 ? { sameAs } : {}),
  };
}

export function websiteSchema(): JsonLd {
  return {
    '@context': CONTEXT,
    '@type': 'WebSite',
    '@id': `${SITE_URL}/#website`,
    name: BRAND,
    url: `${SITE_URL}/`,
    inLanguage: 'en-IN',
    publisher: { '@id': `${SITE_URL}/#organization` },
  };
}

/** FAQPage markup. Entries whose text is still an owner placeholder are left out. */
export function faqSchema(faqs: Pick<Faq, 'q' | 'a'>[]): JsonLd | null {
  const usable = faqs.filter((f) => !isPlaceholder(f.q) && !isPlaceholder(f.a));
  if (usable.length === 0) return null;
  return {
    '@context': CONTEXT,
    '@type': 'FAQPage',
    mainEntity: usable.map((f) => ({
      '@type': 'Question',
      name: f.q,
      acceptedAnswer: { '@type': 'Answer', text: f.a },
    })),
  };
}

export function breadcrumbSchema(items: { name: string; path: string }[]): JsonLd {
  return {
    '@context': CONTEXT,
    '@type': 'BreadcrumbList',
    itemListElement: items.map((item, i) => ({
      '@type': 'ListItem',
      position: i + 1,
      name: item.name,
      item: absoluteUrl(item.path),
    })),
  };
}

/** Loan product markup. Figures are the indicative ranges shown on the page. */
export function loanProductSchema(loan: Loan): JsonLd {
  return {
    '@context': CONTEXT,
    '@type': 'LoanOrCredit',
    name: `${loan.title} in ${SERVICE_AREA}`,
    description: loan.intro,
    url: absoluteUrl(`/loans/${loan.slug}`),
    category: loan.title,
    currency: 'INR',
    interestRate: {
      '@type': 'QuantitativeValue',
      minValue: loan.rateRange.min,
      maxValue: loan.rateRange.max,
      unitText: '% per annum (indicative)',
    },
    amount: {
      '@type': 'MonetaryAmount',
      currency: 'INR',
      minValue: loan.amountRange.min,
      maxValue: loan.amountRange.max,
    },
    loanTerm: {
      '@type': 'QuantitativeValue',
      minValue: loan.tenureMonths.min,
      maxValue: loan.tenureMonths.max,
      unitCode: 'MON',
    },
    areaServed: areaServed(),
    provider: { '@id': `${SITE_URL}/#organization` },
  };
}
