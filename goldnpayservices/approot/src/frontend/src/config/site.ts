export const site = {
  name: 'GoldNPay',
  tagline: 'Find the right loan, with expert help.',
  url: 'https://goldnpay.in',
  phone: '+91 87543 26019',
  phoneE164: '+918754326019', // tel: link
  whatsapp: '918754326019', // digits only, for wa.me
  address: 'GoldNPay, 1st floor, No.23, 12 Cross Street, New Colony, Chrompet, Chennai 600044',
  // Structured form of `address`, used for schema.org markup. Keep both in sync with the Google Business Profile.
  addressDetail: {
    streetAddress: 'GoldNPay, 1st floor, No.23, 12 Cross Street, New Colony, Chrompet',
    locality: 'Chennai',
    region: 'Tamil Nadu',
    postalCode: '600044',
    country: 'IN',
  },
  mapEmbedUrl: '', // TODO(owner): hide the map if empty
  hours: 'Mon–Sat, 9:30 AM – 6:30 PM',
  // Structured form of `hours` for schema.org markup.
  openingHours: {
    days: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
    opens: '09:30',
    closes: '18:30',
  },
  serviceArea: 'Tamil Nadu', // the whole state; the office is in Chennai (see address)
  grievanceOfficer: {
    name: 'Balamohan Krishnan',
    email: 'grievance@goldnpay.in',
    phone: '+91 73582 18024',
    hours: 'Monday to Friday, 10:00 to 17:00',
  },
  // Details used by the Privacy, Terms and Disclaimer pages.
  legal: {
    entityName: 'GoldNPay Services',
    entityType: 'a sole proprietorship',
    gstin: '33ANEPK9580E2ZX',
    lastUpdated: '21 November 2026',
    supportEmail: 'support@goldnpay.in',
    officialPhones: ['+91 73582 18024', '+91 87543 26019'],
    emailDomain: 'goldnpay.in',
    // TODO(owner): customer fee for loan facilitation. Leave empty to say nothing; otherwise one sentence,
    // e.g. 'We do not charge you any fee to process a loan application.'
    loanFeeStatement: '',
    pledgedReleaseServiceCharge: '0.5% per transaction',
    pledgedReleaseWithdrawalFee: '20% of the amount we paid to your lender',
  },
  social: { linkedin: '', instagram: '', facebook: '' }, // hide any that are empty
  foundedYear: 2026, 
  showSampleTestimonials: true, // set to false once real, consented testimonials are in place
  legalReviewed: true, // true hides the "Template — to be reviewed" banner on Privacy, Terms and Disclaimer
} as const;
