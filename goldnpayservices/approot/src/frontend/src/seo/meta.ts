import { site } from '../config/site';
import { loans } from '../data/loans';
import type { Loan } from '../types';

/** Short brand used in page titles. */
export const BRAND = 'GoldNPay';

/** Production origin without a trailing slash. Set VITE_SITE_URL at build time; falls back to site.url. */
export const SITE_URL: string = (
  (import.meta.env?.VITE_SITE_URL as string | undefined) || site.url
).replace(/\/+$/, '');

/** 1200x630 share image served from /public. */
export const OG_IMAGE_PATH = '/og-image.png';

/** Area used in titles and descriptions: the whole state, not just the office city. */
export const SERVICE_AREA: string = site.serviceArea;

export function absoluteUrl(path: string): string {
  if (/^https?:\/\//.test(path)) return path;
  return `${SITE_URL}${path === '/' ? '/' : path.replace(/\/+$/, '')}`;
}

export interface PageMeta {
  title: string;
  description: string;
}

/** Title and description for every static (non-loan-detail) page. Titles stay near 60 and descriptions near 150 characters. */
export const staticPageMeta: Record<string, PageMeta> = {
  '/': {
    title: `Loan Consultant in ${SERVICE_AREA} – Compare Loan Offers | ${BRAND}`,
    description: `Compare personal, home, business and gold loans from 20+ banks and NBFCs in ${SERVICE_AREA}. Free, unbiased expert help. Book your consultation today.`,
  },
  '/loans': {
    title: `Loans in ${SERVICE_AREA} – Personal, Home, Gold & More | ${BRAND}`,
    description: `Explore 7 loan types with indicative rates, eligibility and documents. Compare lenders in ${SERVICE_AREA} and get a free consultation with ${BRAND}.`,
  },
  '/emi-calculator': {
    title: `EMI Calculator – Plan Your Loan Repayment | ${BRAND}`,
    description:
      'Estimate your monthly loan EMI, total interest and repayment schedule for any loan type. Free to use, then talk to an expert about the best offer.',
  },
  '/eligibility': {
    title: `Loan Eligibility Calculator – Check in Seconds | ${BRAND}`,
    description:
      'Check how much loan you may be eligible for based on your income and obligations. It does not affect your credit score. Try it free now.',
  },
  '/consultation': {
    title: `Free Loan Consultation in ${SERVICE_AREA} | ${BRAND}`,
    description: `Tell us what you need and a ${BRAND} loan expert will call you back, compare lender options and guide you from application to disbursal. It is free.`,
  },
  '/about': {
    title: `About ${BRAND} – Your Loan Consultant in ${SERVICE_AREA}`,
    description: `Learn how ${BRAND} helps you compare loans, prepare documents and apply with partner banks and NBFCs. Independent, transparent loan guidance.`,
  },
  '/faq': {
    title: `Loan FAQs – Eligibility, Process & Fees | ${BRAND}`,
    description: `Answers to common questions on loan eligibility, documents, fees and how the process works. Still unsure? Talk to a ${BRAND} expert for free.`,
  },
  '/contact': {
    title: `Contact ${BRAND} – Call, WhatsApp or Visit in ${SERVICE_AREA}`,
    description: `Reach ${BRAND} by phone or WhatsApp, or visit our ${site.addressDetail.locality} office. Share your loan requirement and get a free consultation. We are happy to help.`,
  },
  '/partners': {
    title: `Partner Banks & NBFCs – Disclosure | ${BRAND}`,
    description: `See the indicative list of banks, NBFCs and housing finance companies ${BRAND} works with, and how partner listing and disclosure work.`,
  },
  '/privacy': {
    title: `Privacy Policy | ${BRAND}`,
    description: `How ${BRAND} collects, uses and protects your personal information when you use our website and request a loan consultation.`,
  },
  '/terms': {
    title: `Terms of Use | ${BRAND}`,
    description: `The terms that apply when you use the ${BRAND} website, tools and consultation services. Please read them before you proceed.`,
  },
  '/disclaimer': {
    title: `Disclaimer | ${BRAND}`,
    description: `${BRAND} is a loan consultant, not a lender. Rates and terms shown are indicative and subject to each lender's policy and your credit profile.`,
  },
};

/** "Gold Loan in Chennai – Compare Rates from 8.5% | GoldNPay" */
export function loanMeta(loan: Loan): PageMeta {
  return {
    title: `${loan.title} in ${SERVICE_AREA} – Compare Rates from ${loan.rateRange.min}% | ${BRAND}`,
    description: `Compare ${loan.title.toLowerCase()} offers from top banks and NBFCs in ${SERVICE_AREA}. Indicative rates from ${loan.rateRange.min}% p.a. Get a free expert consultation today.`,
  };
}

/** Every indexable path, in sitemap order. */
export function indexablePaths(): string[] {
  return [...Object.keys(staticPageMeta), ...loans.map((l) => `/loans/${l.slug}`)];
}
