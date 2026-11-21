import type { Loan } from '../types';

// All rates, amounts and tenures are indicative and vary by lender and applicant profile.

const salariedDocs = [
  'PAN card and Aadhaar',
  'Latest 3 months salary slips',
  'Last 6 months bank statements',
  'Current address proof',
];

const selfEmployedDocs = [
  'PAN card and Aadhaar',
  'Last 2 years ITR with computation of income',
  'Last 12 months bank statements',
  'Business proof such as GST registration or licence',
];

export const loans: Loan[] = [
  {
    slug: 'personal-loan',
    title: 'Personal Loan',
    shortDescription: 'Unsecured funds for weddings, travel, medical needs or any personal expense.',
    icon: 'User',
    intro:
      'A personal loan is an unsecured loan you can use for almost any personal need. We help you compare indicative offers from partner lenders and prepare a complete application. Final terms are decided by the lender.',
    idealFor: ['Medical expenses', 'Weddings and celebrations', 'Travel and education', 'Debt consolidation'],
    rateRange: { min: 10.5, max: 24 },
    amountRange: { min: 50000, max: 4000000 },
    tenureMonths: { min: 12, max: 60 },
    eligibility: [
      'Age 21 to 60 years at loan maturity',
      'Stable monthly income from salary or business',
      'Good repayment history on existing loans and cards',
      'Indian resident with valid KYC documents',
    ],
    documents: { salaried: salariedDocs, selfEmployed: selfEmployedDocs },
    features: [
      'No collateral required',
      'Flexible tenure options',
      'Quick documentation support',
      'Use the funds for almost any purpose',
      'Fixed monthly EMI for easy budgeting',
    ],
    faqs: [
      {
        q: 'How much personal loan can I get?',
        a: 'The amount depends on your income, existing obligations and credit profile. Indicatively, lenders offer from ₹50,000 up to ₹40 lakh.',
      },
      {
        q: 'Do I need to give security for a personal loan?',
        a: 'No. A personal loan is unsecured, so no collateral is usually required.',
      },
      {
        q: 'Can I repay the loan early?',
        a: 'Most lenders allow prepayment, sometimes with charges. Check the terms of your chosen lender before you sign.',
      },
    ],
    defaults: { amount: 500000, rate: 11, tenureMonths: 36 },
    seo: {
      title: 'Personal Loan: indicative rates, eligibility and documents',
      description:
        'Learn about personal loans, indicative interest rates, eligibility and documents, and talk to an expert loan consultant.',
    },
  },
  {
    slug: 'home-loan',
    title: 'Home Loan',
    shortDescription: 'Long-tenure financing to buy, build or renovate your home.',
    icon: 'Home',
    intro:
      'A home loan helps you buy, construct or renovate a house over a long tenure. Our consultants explain the options and help you line up the paperwork. Rates shown are indicative and set by the lender.',
    idealFor: ['First-time home buyers', 'Under-construction purchases', 'Home construction', 'Renovation and extension'],
    rateRange: { min: 8.35, max: 11 },
    amountRange: { min: 500000, max: 50000000 },
    tenureMonths: { min: 60, max: 360 },
    eligibility: [
      'Age 21 to 65 years at loan maturity',
      'Steady income that supports the EMI',
      'Property with clear title and approvals',
      'Satisfactory credit history',
    ],
    documents: {
      salaried: [...salariedDocs, 'Property documents and sale agreement'],
      selfEmployed: [...selfEmployedDocs, 'Property documents and sale agreement'],
    },
    features: [
      'Tenure up to 30 years',
      'Lower EMIs through long repayment periods',
      'Possible tax benefits under prevailing law',
      'Options for purchase, construction or renovation',
    ],
    faqs: [
      {
        q: 'How much down payment is needed?',
        a: 'Lenders usually finance a part of the property value, and you pay the rest as down payment. The exact share depends on the lender and property.',
      },
      {
        q: 'Can I add a co-applicant?',
        a: 'Yes. A co-applicant with income can improve the eligible amount, subject to lender policy.',
      },
      {
        q: 'Is a fixed or floating rate better?',
        a: 'It depends on your plans and the lender offering. Our consultants can explain the difference for your situation.',
      },
    ],
    defaults: { amount: 2500000, rate: 8.5, tenureMonths: 240 },
    seo: {
      title: 'Home Loan: indicative rates, eligibility and documents',
      description:
        'Explore home loan options with indicative rates, eligibility and documents, and get expert help with your application.',
    },
  },
  {
    slug: 'business-loan',
    title: 'Business Loan',
    shortDescription: 'Working capital and growth funding for small and medium businesses.',
    icon: 'Briefcase',
    intro:
      'A business loan can fund working capital, equipment or expansion. We help you understand which lenders suit your business profile and what to submit. Terms are decided by the lender.',
    idealFor: ['Working capital needs', 'Equipment purchase', 'Business expansion', 'Inventory financing'],
    rateRange: { min: 14, max: 26 },
    amountRange: { min: 100000, max: 5000000 },
    tenureMonths: { min: 12, max: 60 },
    eligibility: [
      'Business operating for a minimum period, usually 2 or more years',
      'Steady turnover and banking activity',
      'Good credit history of owners and business',
      'Valid business registration and KYC',
    ],
    documents: {
      salaried: [...salariedDocs],
      selfEmployed: [...selfEmployedDocs, 'GST returns for the last 12 months'],
    },
    features: [
      'Funds for working capital or growth',
      'Unsecured options for eligible businesses',
      'Flexible tenure choices',
      'Simple documentation support',
    ],
    faqs: [
      {
        q: 'Is collateral required for a business loan?',
        a: 'Many business loans up to certain limits are unsecured. Larger amounts may need security, depending on the lender.',
      },
      {
        q: 'Can new businesses apply?',
        a: 'Most lenders look for a minimum business vintage. Our consultants can suggest options based on your profile.',
      },
      {
        q: 'What affects the interest rate?',
        a: 'Business vintage, turnover, credit history and the loan amount all influence the rate a lender offers.',
      },
    ],
    defaults: { amount: 1000000, rate: 16, tenureMonths: 36 },
    seo: {
      title: 'Business Loan: indicative rates, eligibility and documents',
      description:
        'Understand business loan options, indicative rates, eligibility and documents, and speak to an expert consultant.',
    },
  },
  {
    slug: 'loan-against-property',
    title: 'Loan Against Property',
    shortDescription: 'Borrow a larger amount at a lower rate by pledging residential or commercial property.',
    icon: 'Building2',
    intro:
      'A loan against property lets you raise funds by pledging a property you own. It suits larger needs such as business expansion or education. Lenders decide the final amount and rate after property valuation.',
    idealFor: ['Business expansion', 'Education expenses', 'Large medical costs', 'Debt consolidation'],
    rateRange: { min: 9, max: 14 },
    amountRange: { min: 500000, max: 50000000 },
    tenureMonths: { min: 60, max: 180 },
    eligibility: [
      'Owner of a residential or commercial property with clear title',
      'Regular income to support repayment',
      'Satisfactory credit history',
      'Age within lender limits at maturity',
    ],
    documents: {
      salaried: [...salariedDocs, 'Property ownership and title documents'],
      selfEmployed: [...selfEmployedDocs, 'Property ownership and title documents'],
    },
    features: [
      'Higher loan amounts than unsecured loans',
      'Rates are generally lower than unsecured loans',
      'Long repayment tenure',
      'Funds can be used for many purposes',
      'You continue to use the property',
    ],
    faqs: [
      {
        q: 'Which properties are accepted?',
        a: 'Lenders usually accept self-occupied or rented residential and commercial properties with clear title. Policies vary.',
      },
      {
        q: 'How is the loan amount decided?',
        a: 'It is a percentage of the property value as assessed by the lender, limited by your repayment capacity.',
      },
      {
        q: 'Do I keep using my property?',
        a: 'Yes. The property is mortgaged to the lender, but you continue to own and use it while you repay.',
      },
    ],
    defaults: { amount: 3000000, rate: 10, tenureMonths: 120 },
    seo: {
      title: 'Loan Against Property: indicative rates and eligibility',
      description:
        'Learn how a loan against property works, with indicative rates, eligibility and documents, and get expert guidance.',
    },
  },
  {
    slug: 'gold-loan',
    title: 'Gold Loan',
    shortDescription: 'Quick short-tenure funds against your gold ornaments with minimal paperwork.',
    icon: 'Coins',
    intro:
      'A gold loan lets you borrow against gold ornaments you own, with light paperwork. It suits short-term cash needs. The amount depends on the weight and purity of your gold and the lender policy.',
    idealFor: ['Short-term cash needs', 'Emergency expenses', 'Farm and business working capital', 'Small-ticket borrowing'],
    rateRange: { min: 8.5, max: 18 },
    amountRange: { min: 10000, max: 5000000 },
    tenureMonths: { min: 3, max: 36 },
    eligibility: [
      'Age 18 years or above',
      'Ownership of gold ornaments of acceptable purity',
      'Valid identity and address proof',
    ],
    documents: {
      salaried: ['PAN card and Aadhaar', 'Current address proof', 'Recent passport-size photograph'],
      selfEmployed: ['PAN card and Aadhaar', 'Current address proof', 'Recent passport-size photograph'],
    },
    features: [
      'Minimal documentation',
      'Short tenures starting from 3 months',
      'Multiple repayment options with some lenders',
      'No income proof needed with many lenders',
    ],
    faqs: [
      {
        q: 'How is my gold valued?',
        a: 'The lender tests the purity and weighs the gold, then offers a loan up to a set share of its value.',
      },
      {
        q: 'Is my gold kept safe?',
        a: 'Lenders store pledged gold in secured vaults and return it once the loan is repaid. Ask the lender about insurance.',
      },
      {
        q: 'Can I repay before the end of the tenure?',
        a: 'Many lenders allow part or full prepayment. Check the terms and any applicable charges.',
      },
    ],
    defaults: { amount: 200000, rate: 9.5, tenureMonths: 12 },
    seo: {
      title: 'Gold Loan: indicative rates, eligibility and documents',
      description:
        'Understand gold loans, indicative interest rates, eligibility and documents, and speak to an expert consultant.',
    },
  },
  {
    slug: 'car-loan',
    title: 'Car Loan',
    shortDescription: 'Finance a new or used car with flexible tenures and affordable EMIs.',
    icon: 'Car',
    intro:
      'A car loan helps you buy a new or pre-owned vehicle and spread the cost over monthly EMIs. We help you compare partner options and complete the paperwork. Terms are set by the lender.',
    idealFor: ['New car buyers', 'Pre-owned car purchase', 'Upgrading your vehicle', 'Salaried and self-employed buyers'],
    rateRange: { min: 8.7, max: 14 },
    amountRange: { min: 100000, max: 10000000 },
    tenureMonths: { min: 12, max: 84 },
    eligibility: [
      'Age 21 to 65 years at loan maturity',
      'Regular income from salary or business',
      'Satisfactory credit history',
      'Valid KYC and address proof',
    ],
    documents: {
      salaried: [...salariedDocs, 'Vehicle quotation or proforma invoice'],
      selfEmployed: [...selfEmployedDocs, 'Vehicle quotation or proforma invoice'],
    },
    features: [
      'Financing for new and used cars',
      'Tenure up to 7 years',
      'Fixed EMIs',
      'Support with documentation',
    ],
    faqs: [
      {
        q: 'How much of the car price can be financed?',
        a: 'Lenders typically finance a share of the on-road price. The share varies by lender and your profile.',
      },
      {
        q: 'Can I get a loan for a used car?',
        a: 'Yes, many lenders finance pre-owned cars, usually with shorter tenures and different rates.',
      },
      {
        q: 'Is insurance mandatory?',
        a: 'Lenders require valid vehicle insurance for the duration of the loan.',
      },
    ],
    defaults: { amount: 800000, rate: 9, tenureMonths: 60 },
    seo: {
      title: 'Car Loan: indicative rates, eligibility and documents',
      description:
        'Explore car loan options with indicative rates, eligibility and documents, and get help from an expert consultant.',
    },
  },
  {
    slug: 'balance-transfer',
    title: 'Balance Transfer',
    shortDescription: 'Move an existing loan to a new lender to reduce your interest cost or EMI.',
    icon: 'ArrowRightLeft',
    intro:
      'A balance transfer moves your existing loan to another lender, often to lower the interest rate or EMI. Savings depend on processing fees and terms, so we help you compare carefully. Terms are set by the new lender.',
    idealFor: ['Borrowers paying a higher rate', 'Home loan holders', 'Those seeking a top-up', 'Improving repayment terms'],
    rateRange: { min: 8.35, max: 16 },
    amountRange: { min: 100000, max: 50000000 },
    tenureMonths: { min: 12, max: 360 },
    eligibility: [
      'An existing loan with a good repayment record',
      'Minimum number of EMIs already paid, as set by the lender',
      'Steady income to support the EMI',
      'Valid KYC and loan statements',
    ],
    documents: {
      salaried: [...salariedDocs, 'Existing loan statement and foreclosure letter'],
      selfEmployed: [...selfEmployedDocs, 'Existing loan statement and foreclosure letter'],
    },
    features: [
      'Possible reduction in interest rate or EMI',
      'Option of a top-up loan with some lenders',
      'Applicable to home, personal and other loans',
      'Help comparing fees and savings',
    ],
    faqs: [
      {
        q: 'Will I really save money?',
        a: 'It depends on the new rate, remaining tenure and processing or foreclosure charges. Compare the total cost before moving.',
      },
      {
        q: 'Are there charges for a transfer?',
        a: 'Lenders may charge processing fees, and the current lender may charge foreclosure fees. Check both.',
      },
      {
        q: 'Can I take a top-up along with the transfer?',
        a: 'Some lenders offer a top-up on eligible loans. Availability depends on lender policy.',
      },
    ],
    defaults: { amount: 2000000, rate: 9, tenureMonths: 180 },
    seo: {
      title: 'Loan Balance Transfer: indicative rates and eligibility',
      description:
        'Learn how a loan balance transfer works, with indicative rates, eligibility and documents, and get expert guidance.',
    },
  },
];

export function getLoanBySlug(slug: string): Loan | undefined {
  return loans.find((loan) => loan.slug === slug);
}
