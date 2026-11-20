import type { Testimonial } from '../types';

// TODO(owner): replace with real, consented testimonials before launch
// These are sample entries with invented names. The UI shows a "Sample" label while
// site.showSampleTestimonials is true.
export const testimonials: Testimonial[] = [
  {
    name: 'Sample Customer A',
    city: 'Chennai',
    loanType: 'Personal Loan',
    quote: 'The consultant explained every step clearly and helped me sort out my documents.',
    rating: 5,
  },
  {
    name: 'Sample Customer B',
    city: 'Coimbatore',
    loanType: 'Home Loan',
    quote: 'I understood my options before applying, and the process felt organised.',
    rating: 4,
  },
  {
    name: 'Sample Customer C',
    city: 'Madurai',
    loanType: 'Business Loan',
    quote: 'Helpful guidance on which documents to prepare for my business loan.',
    rating: 5,
  },
];
