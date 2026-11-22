// ═══════════════════════════════════════════════════════
//  Build-time prerender (runs after `vite build` and `vite build --ssr`).
//
//  Renders every public route to static HTML so search engines and social
//  crawlers get real content, per-page <title>/<meta>/structured data, then
//  writes:
//    dist/<route>/index.html   one file per page (dist/index.html for "/")
//    dist/404.html             the Not Found page (served with status 404)
//    dist/sitemap.xml          every indexable page
//    dist/robots.txt           allow all + the Sitemap line
//
//  The browser bundle then hydrates the HTML (see src/main.tsx).
// ═══════════════════════════════════════════════════════
import fs from 'node:fs';
import path from 'node:path';
import { Writable } from 'node:stream';
import { fileURLToPath, pathToFileURL } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const distDir = path.join(root, 'dist');
const ssrEntry = path.join(root, 'dist-ssr', 'entry-server.js');

const templatePath = path.join(distDir, 'index.html');
const template = fs.readFileSync(templatePath, 'utf8');
for (const marker of ['<!--app-head-->', '<!--app-html-->']) {
  if (!template.includes(marker)) {
    throw new Error(`dist/index.html is missing ${marker}; Vite may have stripped it`);
  }
}

const { renderApp, indexablePaths, SITE_URL } = await import(pathToFileURL(ssrEntry).href);

if (/example\.com/.test(SITE_URL)) {
  console.warn(
    `\nWARNING: canonical URLs and the sitemap use ${SITE_URL}. ` +
      'Set VITE_SITE_URL (or site.url) to the production domain before deploying.\n',
  );
}

async function renderPage(url) {
  const chunks = [];
  const sink = new Writable({
    write(chunk, _enc, cb) {
      chunks.push(Buffer.from(chunk));
      cb();
    },
  });
  const finished = new Promise((resolve, reject) => {
    sink.on('finish', resolve);
    sink.on('error', reject);
  });
  const helmet = await renderApp(url, sink);
  await finished;
  const html = Buffer.concat(chunks).toString('utf8');
  const head = [helmet.title, helmet.priority, helmet.meta, helmet.link, helmet.script]
    .map((tag) => tag.toString())
    .join('\n    ');
  // Function replacers: the rendered HTML must not be interpreted for "$&" style patterns.
  return template
    .replace(/<title>[\s\S]*?<\/title>\s*/, '') // default tags from index.html
    .replace(/<meta name="description"[^>]*>\s*/, '')
    .replace('<!--app-head-->', () => head).replace('<!--app-html-->', () => html);
}

function outFile(route) {
  if (route === '/') return path.join(distDir, 'index.html');
  return path.join(distDir, ...route.split('/').filter(Boolean), 'index.html');
}

function write(file, content) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, content);
}

const routes = indexablePaths();
for (const route of routes) {
  const page = await renderPage(route);
  if (!/<title[^>]*>[^<]+<\/title>/.test(page)) throw new Error(`No <title> rendered for ${route}`);
  if (!page.includes('rel="canonical"')) throw new Error(`No canonical link rendered for ${route}`);
  write(outFile(route), page);
}
// "/__not-found" matches the catch-all route, so this is the Not Found page.
write(path.join(distDir, '404.html'), await renderPage('/__not-found'));

// ---------- sitemap.xml ----------
const lastmod = process.env.SITEMAP_LASTMOD || new Date().toISOString().slice(0, 10);
const urls = routes
  .map((route) => {
    const loc = route === '/' ? `${SITE_URL}/` : `${SITE_URL}${route}`;
    return `  <url>\n    <loc>${loc}</loc>\n    <lastmod>${lastmod}</lastmod>\n  </url>`;
  })
  .join('\n');
write(
  path.join(distDir, 'sitemap.xml'),
  `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${urls}\n</urlset>\n`,
);

// ---------- robots.txt ----------
write(path.join(distDir, 'robots.txt'), `User-agent: *\nAllow: /\n\nSitemap: ${SITE_URL}/sitemap.xml\n`);

console.log(`Prerendered ${routes.length} pages + 404.html, sitemap.xml and robots.txt (${SITE_URL})`);
