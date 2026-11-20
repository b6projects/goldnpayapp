import Card from '../ui/Card';
import LinkButton from '../ui/LinkButton';
import { getLoanBySlug } from '../../data/loans';
import type { LoanSlug } from '../../types';

export interface ConsultationFormProps {
  defaultLoan?: LoanSlug;
  lockLoan?: boolean;
  defaultAmount?: number;
  compact?: boolean;
}

/**
 * Placeholder with the final prop contract; replaced by the real form in GPS-9 (M8).
 * Collects no data. Links to the consultation page with the loan preselected.
 */
export default function ConsultationForm({ defaultLoan, lockLoan = false }: ConsultationFormProps) {
  const loan = defaultLoan ? getLoanBySlug(defaultLoan) : undefined;
  const to = loan ? `/consultation?loan=${loan.slug}` : '/consultation';
  return (
    <Card className="space-y-4" aria-labelledby="consultation-form-heading">
      <h2 id="consultation-form-heading" className="text-xl font-bold text-text">
        Get a free consultation
      </h2>
      {loan && (
        <p className="text-base text-text">
          Loan type:{' '}
          <strong data-testid="consultation-locked-loan" data-locked={lockLoan ? 'true' : 'false'}>
            {loan.title}
          </strong>
        </p>
      )}
      <LinkButton to={to}>Request a consultation</LinkButton>
    </Card>
  );
}
