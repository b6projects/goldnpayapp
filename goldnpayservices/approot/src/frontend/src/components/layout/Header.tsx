import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import { ChevronDown, Menu } from 'lucide-react';
import { loans } from '../../data/loans';
import { site } from '../../config/site';
import LinkButton from '../ui/LinkButton';
import MobileNav from './MobileNav';

const navClass =
  'rounded-xl px-3 py-2 text-base font-medium text-text hover:bg-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 aria-[current=page]:text-primary';

const links: Array<[string, string]> = [
  ['/emi-calculator', 'EMI Calculator'],
  ['/eligibility', 'Eligibility'],
  ['/about', 'About'],
  ['/contact', 'Contact'],
];

function LoansMenu() {
  const [open, setOpen] = useState(false);
  const wrapRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const { pathname } = useLocation();

  useEffect(() => {
    setOpen(false);
  }, [pathname]);

  useEffect(() => {
    if (!open) return;
    function onDown(e: MouseEvent) {
      if (wrapRef.current && !wrapRef.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', onDown);
    return () => document.removeEventListener('mousedown', onDown);
  }, [open]);

  return (
    <div
      ref={wrapRef}
      className="relative"
      onKeyDown={(e) => {
        if (e.key === 'Escape' && open) {
          setOpen(false);
          buttonRef.current?.focus();
        }
      }}
      onBlur={(e) => {
        const next = e.relatedTarget as Node | null;
        if (next && !e.currentTarget.contains(next)) setOpen(false);
      }}
    >
      <button
        ref={buttonRef}
        type="button"
        aria-expanded={open}
        aria-controls="loans-menu"
        onClick={() => setOpen((v) => !v)}
        className={`${navClass} inline-flex items-center gap-1`}
      >
        Loans
        <ChevronDown aria-hidden="true" className="h-4 w-4" />
      </button>
      {open && (
        <ul
          id="loans-menu"
          className="absolute left-0 top-full z-50 mt-1 w-64 rounded-xl border border-surface bg-background p-2 shadow-lg"
        >
          {loans.map((loan) => (
            <li key={loan.slug}>
              <Link to={`/loans/${loan.slug}`} className={`block ${navClass}`}>
                {loan.title}
              </Link>
            </li>
          ))}
          <li>
            <Link to="/loans" className={`block ${navClass}`}>
              All loans
            </Link>
          </li>
        </ul>
      )}
    </div>
  );
}

export default function Header() {
  const [mobileOpen, setMobileOpen] = useState(false);
  return (
    <header className="sticky top-0 z-40 border-b border-surface bg-background">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-4">
        <Link
          to="/"
          className="rounded-xl font-heading text-lg font-semibold text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        >
          {site.name}
        </Link>
        <nav aria-label="Primary" className="hidden items-center gap-1 lg:flex">
          <LoansMenu />
          {links.map(([to, label]) => (
            <NavLink key={to} to={to} className={navClass}>
              {label}
            </NavLink>
          ))}
          <LinkButton to="/consultation" size="sm" className="ml-2">
            Free Consultation
          </LinkButton>
        </nav>
        <button
          type="button"
          aria-label="Open menu"
          aria-expanded={mobileOpen}
          aria-controls="mobile-nav"
          onClick={() => setMobileOpen(true)}
          className="flex h-11 w-11 items-center justify-center rounded-xl text-text hover:bg-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 lg:hidden"
        >
          <Menu aria-hidden="true" className="h-6 w-6" />
        </button>
      </div>
      <MobileNav open={mobileOpen} onClose={() => setMobileOpen(false)} />
    </header>
  );
}
