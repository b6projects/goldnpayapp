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
    name: 'TODO(owner)',
    email: 'grievance@example.com', // TODO(owner)
    phone: '+91 90000 00000', // TODO(owner)
  },
  social: { linkedin: '', instagram: '', facebook: '' }, // hide any that are empty
  foundedYear: 2024, // TODO(owner)
  showSampleTestimonials: true, // set to false once real, consented testimonials are in place
  legalReviewed: false, // set to true after legal review of Privacy, Terms and Disclaimer
} as const;
