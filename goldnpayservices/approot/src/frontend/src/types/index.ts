export type RoutePath = string;

export const LOAN_SLUGS = [
  'personal-loan',
  'home-loan',
  'business-loan',
  'loan-against-property',
  'gold-loan',
  'car-loan',
  'balance-transfer',
] as const;

export type LoanSlug = (typeof LOAN_SLUGS)[number];

export interface Loan {
  slug: LoanSlug;
  title: string;
  shortDescription: string; // max 120 chars, shown on cards
  icon: string; // lucide icon name
  intro: string; // 2-3 sentence paragraph
  idealFor: string[];
  rateRange: { min: number; max: number }; // % per annum, indicative
  amountRange: { min: number; max: number }; // INR
  tenureMonths: { min: number; max: number };
  eligibility: string[];
  documents: { salaried: string[]; selfEmployed: string[] };
  features: string[];
  faqs: { q: string; a: string }[];
  defaults: { amount: number; rate: number; tenureMonths: number }; // EMI calculator start values
  seo: { title: string; description: string };
}

export interface Partner {
  name: string;
  logo: string;
  type: 'Bank' | 'NBFC' | 'HFC';
}

export interface Testimonial {
  name: string;
  city: string;
  loanType: string;
  quote: string;
  rating: 1 | 2 | 3 | 4 | 5;
}

export interface Faq {
  q: string;
  a: string;
  category: 'General' | 'Process' | 'Fees' | 'Eligibility';
}

export interface Stat {
  label: string;
  value: string;
}

export interface HomeStep {
  title: string;
  description: string;
}

export interface WhyUsPoint {
  title: string;
  description: string;
}
