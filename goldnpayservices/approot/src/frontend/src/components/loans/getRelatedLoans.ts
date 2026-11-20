import { loans } from '../../data/loans';
import type { Loan } from '../../types';

/** The 3 loans following the current one in catalogue order, wrapping around. */
export function getRelatedLoans(current: Loan, count = 3): Loan[] {
  const index = loans.findIndex((l) => l.slug === current.slug);
  const result: Loan[] = [];
  for (let i = 1; result.length < count && i < loans.length; i++) {
    result.push(loans[(index + i) % loans.length]);
  }
  return result;
}
