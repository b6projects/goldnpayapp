/// <reference types="vite/client" />
import { describe, expect, it } from 'vitest';

const sources = import.meta.glob(['/src/config/site.ts', '/src/data/*.ts', '/src/types/index.ts'], {
  query: '?raw',
  import: 'default',
  eager: true,
}) as Record<string, string>;

const ownerDoc = Object.values(
  import.meta.glob('/OWNER_INPUTS.md', { query: '?raw', import: 'default', eager: true }) as Record<
    string,
    string
  >,
)[0];

const withMarker = Object.entries(sources)
  .filter(([path, text]) => !path.endsWith('.test.ts') && text.includes('TODO(owner)'))
  .map(([path]) => path.replace(/^\//, ''))
  .sort();

describe('owner inputs (AC3)', () => {
  it('checked-in OWNER_INPUTS.md exists, documents the marker and the grep command', () => {
    expect(ownerDoc).toBeTruthy();
    expect(ownerDoc).toContain('TODO(owner)');
    expect(ownerDoc).toContain('grep -rn "TODO(owner)" src');
  });

  it('finds the marker in the expected source files', () => {
    expect(withMarker).toEqual(
      [
        'src/config/site.ts',
        'src/data/faqs.ts',
        'src/data/partners.ts',
        'src/data/stats.ts',
        'src/data/testimonials.ts',
      ].sort(),
    );
  });

  it('lists every file containing the marker in OWNER_INPUTS.md', () => {
    withMarker.forEach((file) => expect(ownerDoc, file).toContain(file.split('/').pop() as string));
  });
});
