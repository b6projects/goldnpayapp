/// <reference types="vite/client" />
import { describe, expect, it } from 'vitest';
import { indexablePaths } from './meta';

const files = import.meta.glob(['/public/sitemap.xml', '/public/robots.txt', '/index.html'], {
  query: '?raw',
  import: 'default',
  eager: true,
}) as Record<string, string>;

describe('static fallbacks served before/without the build output', () => {
  it('public/sitemap.xml lists every indexable page on goldnpay.in', () => {
    const locs = [...files['/public/sitemap.xml']!.matchAll(/<loc>(.*?)<\/loc>/g)].map((m) => m[1]);
    expect(locs.slice().sort()).toEqual(
      indexablePaths()
        .map((p) => (p === '/' ? 'https://goldnpay.in/' : `https://goldnpay.in${p}`))
        .sort(),
    );
  });

  it('public/robots.txt points to the sitemap', () => {
    expect(files['/public/robots.txt']).toContain('Sitemap: https://goldnpay.in/sitemap.xml');
  });

  it('index.html keeps a default title and description', () => {
    expect(files['/index.html']).toMatch(/<title>[^<]+<\/title>/);
    expect(files['/index.html']).toMatch(/<meta name="description" content="[^"]{50,}"/);
  });
});
