import type { HomeStep, WhyUsPoint } from '../types';

export const howItWorksSteps: HomeStep[] = [
  { title: 'Share your requirement', description: 'Tell us the loan type, amount and your basic profile.' },
  { title: 'We compare lenders', description: 'We review indicative options from our partner banks and NBFCs.' },
  { title: 'We help with documents', description: 'We guide you on the documents and the application form.' },
  { title: 'Get disbursed', description: 'The lender decides on your application and disburses if approved.' },
];

export const whyUsPoints: WhyUsPoint[] = [
  { title: 'Multiple lenders in one place', description: 'Compare options from many banks and NBFCs without visiting each one.' },
  // TODO(owner): confirm the fee policy before keeping the "No-cost consultation" claim.
  { title: 'No-cost consultation', description: 'Talk to an expert about your options before you decide.' },
  { title: 'Expert document support', description: 'We help you prepare complete, correct paperwork.' },
  { title: 'Transparent and secure', description: 'Clear communication and careful handling of your information.' },
];
