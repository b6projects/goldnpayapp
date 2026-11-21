import type { Partner } from '../../types';

/** Name-based tile: a monogram plus the bank name. Uses theme colours only, so no logos are reproduced. */
export default function BankBadge({ partner }: { partner: Partner }) {
  const isPublic = partner.sector === 'Public';
  return (
    <span className="inline-flex items-center gap-3 rounded-xl border border-surface bg-background px-4 py-2.5 shadow-sm transition-all duration-300 hover:-translate-y-0.5 hover:border-accent/60 hover:shadow-md motion-reduce:transition-none motion-reduce:hover:translate-y-0">
      <span
        aria-hidden="true"
        className={`flex h-10 min-w-[2.5rem] items-center justify-center rounded-lg px-1.5 text-xs font-bold tracking-wide ${
          isPublic ? 'bg-primary text-sand' : 'bg-accent text-white'
        }`}
      >
        {partner.short}
      </span>
      <span className="whitespace-nowrap text-sm font-semibold text-text">{partner.name}</span>
    </span>
  );
}
