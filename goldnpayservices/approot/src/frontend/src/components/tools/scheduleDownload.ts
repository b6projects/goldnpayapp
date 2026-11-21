import { buildScheduleTable, type ScheduleExportInput } from '../../lib/scheduleTable';

const inr = (n: number) => new Intl.NumberFormat('en-IN').format(n);

/** Downloads the repayment schedule as a PDF. The PDF library loads only when this is called. */
export async function downloadSchedulePdf(input: ScheduleExportInput): Promise<void> {
  const [{ jsPDF }, { default: autoTable }] = await Promise.all([
    import('jspdf'),
    import('jspdf-autotable'),
  ]);
  const t = buildScheduleTable(input);
  const doc = new jsPDF({ unit: 'pt', format: 'a4' });
  doc.setFontSize(16);
  doc.text(t.title, 40, 50);
  doc.setFontSize(10);
  let y = 72;
  for (const [label, value] of t.details) {
    const text =
      typeof value === 'number' && label.includes('INR') ? `Rs. ${inr(value)}` : String(value);
    doc.text(`${label.replace(' (INR)', '')}: ${text}`, 40, y);
    y += 14;
  }
  const money = (n: number) => inr(n);
  const common = {
    theme: 'grid' as const,
    styles: { fontSize: 9, halign: 'right' as const },
    headStyles: {
      fillColor: [17, 17, 17] as [number, number, number],
      textColor: [212, 175, 55] as [number, number, number],
    },
  };
  autoTable(doc, {
    ...common,
    startY: y + 8,
    head: [t.yearlyHeaders.map((h, i) => (i === 0 ? h : `${h} (Rs.)`))],
    body: t.yearlyRows.map((row) => row.map((v, i) => (i === 0 ? String(v) : money(v)))),
  });
  autoTable(doc, {
    ...common,
    head: [t.monthlyHeaders.map((h, i) => (i === 0 ? h : `${h} (Rs.)`))],
    body: t.monthlyRows.map((row) => row.map((v, i) => (i === 0 ? String(v) : money(v)))),
    margin: { top: 40 },
  });
  doc.setFontSize(8);
  const last = doc.getNumberOfPages();
  for (let p = 1; p <= last; p += 1) {
    doc.setPage(p);
    doc.text(
      'Indicative estimate. Actual rate and EMI depend on the lender.',
      40,
      doc.internal.pageSize.getHeight() - 20,
    );
  }
  doc.save(`${t.fileBaseName}.pdf`);
}

/** Downloads the repayment schedule as an .xlsx workbook (monthly and yearly sheets). */
export async function downloadScheduleExcel(input: ScheduleExportInput): Promise<void> {
  const { default: writeExcelFile } = await import('write-excel-file/browser');
  const t = buildScheduleTable(input);
  const head = (labels: string[]) =>
    labels.map((value) => ({ value, fontWeight: 'bold' as const }));
  const num = (v: number, i: number) => (i === 0 ? v : { value: v, format: '#,##0' });
  const summary = [
    [{ value: t.title, fontWeight: 'bold' as const }],
    ...t.details.map(([label, value]) => [label, value]),
  ];
  await writeExcelFile([
    {
      data: [...summary, [], head(t.yearlyHeaders), ...t.yearlyRows.map((r) => r.map(num))],
      sheet: 'Summary',
      columns: [{ width: 26 }, { width: 18 }, { width: 18 }, { width: 18 }],
    },
    {
      data: [head(t.monthlyHeaders), ...t.monthlyRows.map((r) => r.map(num))],
      sheet: 'Monthly schedule',
      columns: [{ width: 10 }, { width: 14 }, { width: 14 }, { width: 14 }, { width: 18 }],
      stickyRowsCount: 1,
    },
  ]).toFile(`${t.fileBaseName}.xlsx`);
}
