import { useEffect, useRef } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { X } from 'lucide-react';
import { loans } from '../../data/loans';
import { site } from '../../config/site';
import LinkButton from '../ui/LinkButton';

export interface MobileNavProps {
  open: boolean;
  onClose: () => void;
}

const FOCUSABLE = 'a[href], button:not([disabled]), [tabindex]:not([tabindex="-1"])';

const linkClass =
  'block rounded-xl px-3 py-3 text-base font-medium text-text hover:bg-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2';

const mainLinks: Array<[string, string]> = [
  ['/emi-calculator', 'EMI Calculator'],
  ['/eligibility', 'Eligibility'],
  ['/about', 'About'],
  ['/contact', 'Contact'],
];

export default function MobileNav({ open, onClose }: MobileNavProps) {
  if (!open) return null;
  return <Drawer onClose={onClose} />;
}

function Drawer({ onClose }: { onClose: () => void }) {
  const panelRef = useRef<HTMLDivElement>(null);
  const closeRef = useRef<HTMLButtonElement>(null);
  const { pathname } = useLocation();
  const initialPath = useRef(pathname);
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose;

  // Save focus, lock scroll, move focus in; restore both on cleanup (close or unmount).
  useEffect(() => {
    const previous = document.activeElement as HTMLElement | null;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    closeRef.current?.focus();
    return () => {
      document.body.style.overflow = previousOverflow;
      previous?.focus();
    };
  }, []);

  // Close on route change.
  useEffect(() => {
    if (pathname !== initialPath.current) onCloseRef.current();
  }, [pathname]);

  function onKeyDown(e: React.KeyboardEvent<HTMLDivElement>) {
    if (e.key === 'Escape') {
      e.stopPropagation();
      onClose();
      return;
    }
    if (e.key !== 'Tab') return;
    const items = panelRef.current?.querySelectorAll<HTMLElement>(FOCUSABLE);
    if (!items || items.length === 0) return;
    const first = items[0];
    const last = items[items.length - 1];
    const active = document.activeElement;
    if (e.shiftKey && active === first) {
      e.preventDefault();
      last.focus();
    } else if (!e.shiftKey && active === last) {
      e.preventDefault();
      first.focus();
    }
  }

  return (
    <div
      id="mobile-nav"
      role="dialog"
      aria-modal="true"
      aria-label="Main menu"
      className="fixed inset-0 z-50 lg:hidden"
      onKeyDown={onKeyDown}
    >
      <div
        data-testid="mobile-nav-backdrop"
        className="absolute inset-0 bg-text/50"
        onClick={onClose}
      />
      <div
        ref={panelRef}
        className="absolute right-0 top-0 flex h-full w-80 max-w-full flex-col gap-2 overflow-y-auto bg-background p-4 shadow-xl"
      >
        <div className="flex items-center justify-between">
          <img src="/images/logo.png" alt={site.name} width={48} height={48} className="h-12 w-12" />
          <button
            ref={closeRef}
            type="button"
            aria-label="Close menu"
            onClick={onClose}
            className="flex h-11 w-11 items-center justify-center rounded-xl text-text hover:bg-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          >
            <X aria-hidden="true" className="h-6 w-6" />
          </button>
        </div>
        <nav aria-label="Mobile">
          <ul className="mt-2 space-y-1">
            {mainLinks.map(([to, label]) => (
              <li key={to}>
                <NavLink to={to} className={linkClass}>
                  {label}
                </NavLink>
              </li>
            ))}
          </ul>
          <p className="mt-4 px-3 text-sm font-semibold text-muted">Loans</p>
          <ul className="space-y-1">
            {loans.map((loan) => (
              <li key={loan.slug}>
                <NavLink to={`/loans/${loan.slug}`} className={linkClass}>
                  {loan.title}
                </NavLink>
              </li>
            ))}
            <li>
              <NavLink to="/loans" end className={linkClass}>
                All loans
              </NavLink>
            </li>
          </ul>
        </nav>
        <div className="mt-4">
          <LinkButton to="/consultation" className="w-full">
            Free Consultation
          </LinkButton>
        </div>
      </div>
    </div>
  );
}
