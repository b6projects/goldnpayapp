import { useId, useMemo, useState } from 'react';
import { ChevronDown, ChevronRight, FileSpreadsheet, FileText } from 'lucide-react';
import { downloadScheduleExcel, downloadSchedulePdf } from './scheduleDownload';
import { buildSchedule, summarizeByYear } from '../../lib/emi';
import { formatINR } from '../../lib/format';

export interface AmortizationTableProps {
  principal: number;
  ratePct: number;
  months: number;
  /** Loan name used in the downloaded file title. */
  loanTitle?: string;
}

const iconClass =
  'inline-flex h-9 w-9 items-center justify-center rounded-lg hover:bg-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary disabled:cursor-not-allowed disabled:opacity-60';

const th = 'px-3 py-2 text-right font-semibold first:text-left';
const td = 'px-3 py-2 text-right first:text-left';

function YearBody({
  year,
  open,
  onToggle,
}: {
  year: ReturnType<typeof summarizeByYear>[number];
  open: boolean;
  onToggle: () => void;
}) {
  const monthsId = useId();
  return (
    <>
      <tr className="border-t border-surface">
        <th scope="row" className="px-3 py-2 text-left font-medium">
          <button
            type="button"
            aria-expanded={open}
            aria-controls={monthsId}
            onClick={onToggle}
            className="inline-flex min-h-[44px] items-center gap-1 text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
          >
            {open ? (
              <ChevronDown aria-hidden="true" className="h-4 w-4" />
            ) : (
              <ChevronRight aria-hidden="true" className="h-4 w-4" />
            )}
            Year {year.year}
          </button>
        </th>
        <td className={td}>{formatINR(year.principal)}</td>
        <td className={td}>{formatINR(year.interest)}</td>
        <td className={td}>{formatINR(year.balance)}</td>
      </tr>
      <tr id={monthsId} hidden={!open}>
        <td colSpan={4} className="p-0">
          {open && (
            <table className="w-full text-sm">
              <caption className="sr-only">Monthly breakdown for year {year.year}</caption>
              <thead className="bg-surface text-muted">
                <tr>
                  <th scope="col" className={th}>
                    Month
                  </th>
                  <th scope="col" className={th}>
                    EMI
                  </th>
                  <th scope="col" className={th}>
                    Principal
                  </th>
                  <th scope="col" className={th}>
                    Interest
                  </th>
                  <th scope="col" className={th}>
                    Balance
                  </th>
                </tr>
              </thead>
              <tbody>
                {year.rows.map((row) => (
                  <tr key={row.month} className="border-t border-surface">
                    <td className={td}>{row.month}</td>
                    <td className={td}>{formatINR(row.emi)}</td>
                    <td className={td}>{formatINR(row.principal)}</td>
                    <td className={td}>{formatINR(row.interest)}</td>
                    <td className={td}>{formatINR(row.balance)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </td>
      </tr>
    </>
  );
}

/** Repayment schedule, collapsed by default; yearly rows expand to months. */
export default function AmortizationTable({
  principal,
  ratePct,
  months,
  loanTitle,
}: AmortizationTableProps) {
  const [open, setOpen] = useState(false);
  const [openYear, setOpenYear] = useState<number | null>(null);
  const panelId = useId();
  const [busy, setBusy] = useState<'pdf' | 'excel' | null>(null);
  const [error, setError] = useState('');

  const download = async (kind: 'pdf' | 'excel') => {
    setBusy(kind);
    setError('');
    try {
      const input = { principal, ratePct, months, loanTitle };
      await (kind === 'pdf' ? downloadSchedulePdf(input) : downloadScheduleExcel(input));
    } catch {
      setError('Could not create the file. Please try again.');
    } finally {
      setBusy(null);
    }
  };

  const years = useMemo(
    () => (open ? summarizeByYear(buildSchedule(principal, ratePct, months)) : []),
    [open, principal, ratePct, months],
  );

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-x-4 gap-y-1">
        <div className="flex items-center gap-1">
          <button
            type="button"
            aria-expanded={open}
            aria-controls={panelId}
            onClick={() => setOpen((o) => !o)}
            className="inline-flex min-h-[44px] items-center gap-2 font-medium text-primary underline-offset-4 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
          >
            {open ? (
              <ChevronDown aria-hidden="true" className="h-4 w-4" />
            ) : (
              <ChevronRight aria-hidden="true" className="h-4 w-4" />
            )}
            View repayment schedule
          </button>
          <button
            type="button"
            onClick={() => download('pdf')}
            disabled={busy !== null}
            aria-label="Download PDF"
            title="Download PDF"
            className={iconClass}
          >
            <FileText
              aria-hidden="true"
              className={
                busy === 'pdf' ? 'h-5 w-5 animate-pulse text-brand-pdf' : 'h-5 w-5 text-brand-pdf'
              }
            />
          </button>
          <button
            type="button"
            onClick={() => download('excel')}
            disabled={busy !== null}
            aria-label="Download Excel"
            title="Download Excel"
            className={iconClass}
          >
            <FileSpreadsheet
              aria-hidden="true"
              className={
                busy === 'excel'
                  ? 'h-5 w-5 animate-pulse text-brand-excel'
                  : 'h-5 w-5 text-brand-excel'
              }
            />
          </button>
        </div>
      </div>
      <p role="alert" className="text-sm text-red-700">
        {error}
      </p>
      <div id={panelId} hidden={!open} className="mt-3">
        {open && (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[480px] text-sm">
              <caption className="sr-only">Yearly repayment schedule (indicative)</caption>
              <thead className="bg-surface text-muted">
                <tr>
                  <th scope="col" className={th}>
                    Year
                  </th>
                  <th scope="col" className={th}>
                    Principal
                  </th>
                  <th scope="col" className={th}>
                    Interest
                  </th>
                  <th scope="col" className={th}>
                    Closing balance
                  </th>
                </tr>
              </thead>
              <tbody>
                {years.map((y) => (
                  <YearBody
                    key={y.year}
                    year={y}
                    open={openYear === y.year}
                    onToggle={() => setOpenYear((cur) => (cur === y.year ? null : y.year))}
                  />
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
