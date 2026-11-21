import type { Faq } from '../types';

export const FAQ_CATEGORIES: Faq['category'][] = ['General', 'Process', 'Fees', 'Eligibility'];

/** Case-insensitive, trimmed substring match on the question text. Never a regex. */
export function filterFaqs(items: Faq[], query: string): Faq[] {
  const needle = query.trim().toLowerCase();
  if (needle === '') return items;
  return items.filter((f) => f.q.toLowerCase().includes(needle));
}

/** Groups FAQs in the fixed category order, omitting empty groups. */
export function groupFaqsByCategory(items: Faq[]): [Faq['category'], Faq[]][] {
  const groups: [Faq['category'], Faq[]][] = FAQ_CATEGORIES.map((c) => [
    c,
    items.filter((f) => f.category === c),
  ]);
  return groups.filter(([, list]) => list.length > 0);
}
