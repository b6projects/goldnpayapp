import { buildSchedule, summarize, summarizeByYear } from './emi';
import { formatTenure } from './format';

export interface ScheduleExportInput {
  principal: number;
  ratePct: number;
  months: number;
  /** Optional loan name shown in the title, e.g. "Personal Loan". */
  loanTitle?: string;
}

export interface ScheduleTable {
  title: string;
  /** Label and value pairs describing the loan, shown above the tables. */
  details: Array<[string, number | string]>;
  monthlyHeaders: string[];
  /** Month, EMI, principal, interest, closing balance. Values are rounded to whole rupees. */
  monthlyRows: number[][];
  yearlyHeaders: string[];
  /** Year, principal, interest, closing balance. */
  yearlyRows: number[][];
  fileBaseName: string;
}

const r = Math.round;

/** Builds the data behind the PDF and Excel repayment-schedule downloads. Pure. */
export function buildScheduleTable({
  principal,
  ratePct,
  months,
  loanTitle,
}: ScheduleExportInput): ScheduleTable {
  const rows = buildSchedule(principal, ratePct, months);
  const { emi, totalInterest, totalPayment } = summarize(principal, ratePct, months);
  return {
    title: loanTitle ? `${loanTitle} repayment schedule` : 'Loan repayment schedule',
    details: [
      ['Loan amount (INR)', r(principal)],
      ['Interest rate (% p.a.)', ratePct],
      ['Tenure', formatTenure(months)],
      ['Monthly EMI (INR)', r(emi)],
      ['Total interest (INR)', r(totalInterest)],
      ['Total payment (INR)', r(totalPayment)],
    ],
    monthlyHeaders: ['Month', 'EMI', 'Principal', 'Interest', 'Closing balance'],
    monthlyRows: rows.map((x) => [x.month, r(x.emi), r(x.principal), r(x.interest), r(x.balance)]),
    yearlyHeaders: ['Year', 'Principal', 'Interest', 'Closing balance'],
    yearlyRows: summarizeByYear(rows).map((y) => [
      y.year,
      r(y.principal),
      r(y.interest),
      r(y.balance),
    ]),
    fileBaseName: 'repayment-schedule',
  };
}
