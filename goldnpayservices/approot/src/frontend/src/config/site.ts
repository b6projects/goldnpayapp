export const site = {
  name: 'GoldNPay Services', // TODO(owner): confirm the business name
  tagline: 'Find the right loan, with expert help.',
  url: 'https://example.com', // TODO(owner): production domain
  phone: '+91 90000 00000', // TODO(owner): display format
  phoneE164: '+919000000000', // tel: link
  whatsapp: '919000000000', // digits only, for wa.me. TODO(owner)
  email: 'hello@example.com', // TODO(owner)
  address: 'TODO(owner): Office address, City, State PIN',
  mapEmbedUrl: '', // TODO(owner): hide the map if empty
  hours: 'Mon–Sat, 9:30 AM – 6:30 PM',
  citiesServed: ['Chennai'], // TODO(owner)
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
