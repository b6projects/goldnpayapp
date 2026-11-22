# GoldNPay frontend

Vite + React + TypeScript (strict) + Tailwind CSS v3 (the spec lists a `tailwind.config` file).

## Setup

    npm ci
    npm run dev

## Scripts

dev, build (`tsc --noEmit && vite build`, then a server build and `scripts/prerender.mjs`, which writes static HTML for every page plus `sitemap.xml` and `robots.txt` into `dist`), preview, lint (zero warnings), format, test, test:watch, typecheck.

Colour tokens live only in `tailwind.config.ts`; components use token classes, never raw hex.
Brand palette (Color Hunt 28301c-323d25-9a7e3b-d2cf9a): `primary` #28301C (dark olive, also body text), `secondary` #323D25, `accent` #9A7E3B (bronze gold), `sand` #D2CF9A (primary buttons, active nav); `surface` #EFEEDA is a light tint of sand. Logo: `public/images/logo.png` (also the favicon, `public/favicon.png`).
