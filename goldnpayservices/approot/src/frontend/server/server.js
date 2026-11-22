// ═══════════════════════════════════════════════════════
//  GoldNPay frontend runtime server
//
//  One server for every environment (replaces GoldBucks'
//  server-dev.js / server-uat.js / server-prd.js). All
//  settings come from environment variables:
//
//    APP_ENV      DEV | UAT | PRD         (default DEV; VITE_APP_ENV also accepted)
//    PORT         port to listen on       (default 4173; EXPOSE_PORT also accepted)
//    DIST_DIR     built site folder       (default ../dist next to this file)
//    VITE_*       any variable starting with VITE_ is published to the browser
//                 as window.env.<NAME> via /env-config.js — e.g. VITE_API_HOST.
//                 Never put secrets in VITE_* variables: the browser can read them.
//
//  Routes:
//    GET /env-config.js  runtime config for the browser (not cached)
//    GET /health         liveness check used by the Docker HEALTHCHECK
//    static files        from DIST_DIR (hashed /assets cached for a year)
//    page routes         the prerendered <route>/index.html written by scripts/prerender.mjs
//    anything else       404.html with status 404 (the app then shows its Not Found page)
// ═══════════════════════════════════════════════════════
import express from 'express';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const APP_ENV = (process.env.APP_ENV || process.env.VITE_APP_ENV || 'DEV').toUpperCase();
const PORT = Number(process.env.PORT || process.env.EXPOSE_PORT || 4173);
const DIST_DIR = path.resolve(process.env.DIST_DIR || path.join(__dirname, '..', 'dist'));
const APP_VERSION = process.env.APP_VERSION || 'dev';
const INDEX_FILE = path.join(DIST_DIR, 'index.html');
const NOT_FOUND_FILE = path.join(DIST_DIR, '404.html');

// ---------- Runtime browser config ----------
function buildPublicEnv() {
  const env = {};
  for (const [key, value] of Object.entries(process.env)) {
    if (key.startsWith('VITE_') && value !== undefined) env[key] = value;
  }
  env.VITE_APP_ENV = APP_ENV;
  env.VITE_APP_VERSION = APP_VERSION;
  return env;
}
const publicEnv = buildPublicEnv();
// JSON.stringify output is valid JS; escape "<" so a value can never close a <script> tag.
const envConfigJs = `window.env = ${JSON.stringify(publicEnv).replace(/</g, '\\u003c')};\n`;

// ---------- index.html (read once, fail fast if missing) ----------
let notFoundHtml;
try {
  fs.accessSync(INDEX_FILE);
  // Fall back to index.html if a build predates prerendering.
  notFoundHtml = fs.readFileSync(fs.existsSync(NOT_FOUND_FILE) ? NOT_FOUND_FILE : INDEX_FILE, 'utf8');
} catch (err) {
  console.error(`FATAL: cannot read ${INDEX_FILE}: ${err.message}`);
  process.exit(1);
}

// ---------- App ----------
const app = express();
app.disable('x-powered-by');
app.set('trust proxy', true);

// Basic security headers (no CSP yet: the site loads Google Fonts;
// add one at the reverse proxy once all external origins are known).
app.use((req, res, next) => {
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'SAMEORIGIN');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
  next();
});

app.get('/env-config.js', (req, res) => {
  res.type('application/javascript');
  res.setHeader('Cache-Control', 'no-store');
  res.send(envConfigJs);
});

app.get('/health', (req, res) => {
  res.setHeader('Cache-Control', 'no-store');
  res.json({ status: 'UP', env: APP_ENV, version: APP_VERSION });
});

// Vite puts content-hashed files in /assets, so they can be cached forever.
app.use(
  '/assets',
  express.static(path.join(DIST_DIR, 'assets'), {
    immutable: true,
    maxAge: '365d',
    fallthrough: false,
  }),
);

// Everything else in dist/ (favicon, robots.txt, sitemap.xml, images ...). Page HTML is
// served by the handler below so it is never cached; "redirect: false" stops express from
// redirecting /loans to /loans/ before that handler runs.
app.use(express.static(DIST_DIR, { index: false, redirect: false, maxAge: '1h' }));

// Prerendered pages: /loans/gold-loan is served from dist/loans/gold-loan/index.html.
// Anything that is not a prerendered page gets the Not Found page with a real 404 status
// (so search engines do not index junk URLs). A missing file such as /images/x.png gets a
// plain 404 instead of HTML.
app.use((req, res, next) => {
  if (req.method !== 'GET' && req.method !== 'HEAD') return next();
  if (path.extname(req.path)) return res.status(404).type('text/plain').send('Not found');
  let pathname;
  try {
    pathname = decodeURIComponent(req.path);
  } catch {
    return res.status(400).type('text/plain').send('Bad request');
  }
  const segments = pathname.split('/').filter(Boolean);
  res.setHeader('Cache-Control', 'no-cache');
  // Reject traversal and anything outside DIST_DIR before touching the filesystem.
  const pageFile = path.join(DIST_DIR, ...segments, 'index.html');
  if (!segments.includes('..') && pageFile.startsWith(DIST_DIR + path.sep) && fs.existsSync(pageFile)) {
    return res.type('html').sendFile(pageFile);
  }
  if (segments.length === 0) return res.type('html').sendFile(INDEX_FILE);
  res.status(404).type('html').send(notFoundHtml);
});

// Error handler (e.g. a missing /assets file from fallthrough:false).
// eslint-disable-next-line no-unused-vars
app.use((err, req, res, next) => {
  const status = err.status || err.statusCode || 500;
  if (status >= 500) console.error(err);
  res.status(status).type('text/plain').send(status === 404 ? 'Not found' : 'Server error');
});

const server = app.listen(PORT, () => {
  console.log(`GoldNPay frontend ${APP_VERSION} listening on port ${PORT} (env ${APP_ENV})`);
  console.log(`Serving ${DIST_DIR}`);
  console.log('Browser config keys:', Object.keys(publicEnv).join(', '));
});

// Stop cleanly on `docker stop` / compose down.
for (const signal of ['SIGTERM', 'SIGINT']) {
  process.on(signal, () => {
    console.log(`${signal} received, shutting down`);
    server.close(() => process.exit(0));
    setTimeout(() => process.exit(0), 5000).unref();
  });
}
