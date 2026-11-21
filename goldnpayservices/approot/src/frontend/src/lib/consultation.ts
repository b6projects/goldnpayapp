export interface ConsultationPathInput {
  /** Loan slug; omitted from the query when absent. */
  loan?: string;
  /** Amount in INR; truncated to an integer. Omitted when absent or not positive. */
  amount?: number;
}

/** Builds the consultation CTA link: /consultation?loan=<slug>&amount=<int>. */
export function buildConsultationPath({ loan, amount }: ConsultationPathInput): string {
  const params = new URLSearchParams();
  if (loan) params.set('loan', loan);
  if (amount !== undefined && Number.isFinite(amount) && amount > 0) {
    params.set('amount', String(Math.floor(amount)));
  }
  const query = params.toString();
  return query ? `/consultation?${query}` : '/consultation';
}
