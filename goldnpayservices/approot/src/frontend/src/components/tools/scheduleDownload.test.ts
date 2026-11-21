import { afterEach, describe, expect, it, vi } from 'vitest';
import { downloadScheduleExcel, downloadSchedulePdf } from './scheduleDownload';

const saved: Array<{ name: string; pages: number }> = [];

vi.mock('jspdf', async (importOriginal) => {
  const mod = await importOriginal<typeof import('jspdf')>();
  class TestPdf extends mod.jsPDF {
    constructor(...args: ConstructorParameters<typeof mod.jsPDF>) {
      super(...args);
      // save is an instance method set in the base constructor, so override it here.
      this.save = ((name?: string) => {
        saved.push({ name: name ?? '', pages: this.getNumberOfPages() });
        return this;
      }) as unknown as typeof this.save;
    }
  }
  return { ...mod, jsPDF: TestPdf };
});

const input = { principal: 500000, ratePct: 11, months: 36, loanTitle: 'Personal Loan' };

describe('schedule downloads', () => {
  afterEach(() => vi.restoreAllMocks());

  function captureDownloads() {
    const names: string[] = [];
    const blobs: Blob[] = [];
    vi.stubGlobal(
      'URL',
      Object.assign(URL, {
        createObjectURL: (b: Blob) => {
          blobs.push(b);
          return 'blob:test';
        },
        revokeObjectURL: () => undefined,
      }),
    );
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (
      this: HTMLAnchorElement,
    ) {
      names.push(this.download);
    });
    return { names, blobs };
  }

  it('creates a PDF named repayment-schedule.pdf with several pages', async () => {
    await downloadSchedulePdf(input);
    expect(saved[0].name).toBe('repayment-schedule.pdf');
    expect(saved[0].pages).toBeGreaterThanOrEqual(2);
  });

  it('creates a non-empty xlsx named repayment-schedule.xlsx', async () => {
    const { names, blobs } = captureDownloads();
    await downloadScheduleExcel(input);
    expect(names).toContain('repayment-schedule.xlsx');
    expect(blobs[0].size).toBeGreaterThan(1000);
  });
});
