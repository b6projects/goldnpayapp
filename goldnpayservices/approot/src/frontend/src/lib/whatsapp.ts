/** WhatsApp consultation link building. No logging or storage of details. */

export interface ConsultationDetails {
  name: string;
  phone: string;
  loanType: string;
  amount?: string;
  tenure?: string;
  city?: string;
  message?: string;
}

function present(v: string | undefined): v is string {
  return v !== undefined && v.trim() !== '';
}

export function buildConsultationMessage(details: ConsultationDetails): string {
  const lines: string[] = ['Hello, I would like a loan consultation.'];
  const fields: Array<[string, string | undefined]> = [
    ['Name', details.name],
    ['Phone', details.phone],
    ['Loan type', details.loanType],
    ['Amount', details.amount],
    ['Tenure', details.tenure],
    ['City', details.city],
    ['Message', details.message],
  ];
  for (const [label, value] of fields) {
    if (present(value)) lines.push(`${label}: ${value.trim()}`);
  }
  return lines.join('\n');
}

/** Builds a wa.me link; the business number is a parameter (digits kept only). */
export function buildWhatsAppUrl(phone: string, text: string): string {
  const digits = phone.replace(/\D/g, '');
  return `https://wa.me/${digits}?text=${encodeURIComponent(text)}`;
}
