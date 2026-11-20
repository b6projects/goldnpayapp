# GoldNPay frontend

Vite + React + TypeScript (strict) + Tailwind CSS v3 (the spec lists a `tailwind.config` file).

## Setup

    npm ci
    npm run dev

## Scripts

dev, build (`tsc --noEmit && vite build`), preview, lint (zero warnings), format, test, test:watch, typecheck.

Colour tokens live only in `tailwind.config.ts`; components use token classes, never raw hex.
