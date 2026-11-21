import type { Partner } from '../types';

// TODO(owner): confirm permission to display these bank names (and any partnership wording) before launch.
// Names are shown as text badges only, with no official logos, so no trademark artwork is reproduced.
export const partners: Partner[] = [
  // Public sector banks
  { name: 'State Bank of India', short: 'SBI', type: 'Bank', sector: 'Public' },
  { name: 'Punjab National Bank', short: 'PNB', type: 'Bank', sector: 'Public' },
  { name: 'Bank of Baroda', short: 'BoB', type: 'Bank', sector: 'Public' },
  { name: 'Canara Bank', short: 'CB', type: 'Bank', sector: 'Public' },
  { name: 'Union Bank of India', short: 'UBI', type: 'Bank', sector: 'Public' },
  { name: 'Bank of India', short: 'BoI', type: 'Bank', sector: 'Public' },
  { name: 'Indian Bank', short: 'IB', type: 'Bank', sector: 'Public' },
  { name: 'Central Bank of India', short: 'CBI', type: 'Bank', sector: 'Public' },
  { name: 'Indian Overseas Bank', short: 'IOB', type: 'Bank', sector: 'Public' },
  { name: 'UCO Bank', short: 'UCO', type: 'Bank', sector: 'Public' },
  { name: 'Bank of Maharashtra', short: 'BoM', type: 'Bank', sector: 'Public' },
  { name: 'Punjab & Sind Bank', short: 'PSB', type: 'Bank', sector: 'Public' },
  // Private sector banks
  { name: 'HDFC Bank', short: 'HDFC', type: 'Bank', sector: 'Private' },
  { name: 'ICICI Bank', short: 'ICICI', type: 'Bank', sector: 'Private' },
  { name: 'Axis Bank', short: 'AXIS', type: 'Bank', sector: 'Private' },
  { name: 'Kotak Mahindra Bank', short: 'KMB', type: 'Bank', sector: 'Private' },
  { name: 'IndusInd Bank', short: 'IIB', type: 'Bank', sector: 'Private' },
  { name: 'Yes Bank', short: 'YES', type: 'Bank', sector: 'Private' },
  { name: 'IDFC FIRST Bank', short: 'IDFC', type: 'Bank', sector: 'Private' },
  { name: 'Federal Bank', short: 'FED', type: 'Bank', sector: 'Private' },
  { name: 'South Indian Bank', short: 'SIB', type: 'Bank', sector: 'Private' },
  { name: 'RBL Bank', short: 'RBL', type: 'Bank', sector: 'Private' },
  { name: 'Bandhan Bank', short: 'BDN', type: 'Bank', sector: 'Private' },
  { name: 'IDBI Bank', short: 'IDBI', type: 'Bank', sector: 'Private' },
  { name: 'Karur Vysya Bank', short: 'KVB', type: 'Bank', sector: 'Private' },
  { name: 'City Union Bank', short: 'CUB', type: 'Bank', sector: 'Private' },
  { name: 'CSB Bank', short: 'CSB', type: 'Bank', sector: 'Private' },
  { name: 'DCB Bank', short: 'DCB', type: 'Bank', sector: 'Private' },
  { name: 'Tamilnad Mercantile Bank', short: 'TMB', type: 'Bank', sector: 'Private' },
  { name: 'Dhanlaxmi Bank', short: 'DLB', type: 'Bank', sector: 'Private' },
  { name: 'Jammu & Kashmir Bank', short: 'J&K', type: 'Bank', sector: 'Private' },
  { name: 'Karnataka Bank', short: 'KBL', type: 'Bank', sector: 'Private' },
  { name: 'Nainital Bank', short: 'NTB', type: 'Bank', sector: 'Private' },
];
