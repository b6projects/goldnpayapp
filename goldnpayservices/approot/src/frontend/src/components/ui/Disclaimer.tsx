import type { ReactNode } from 'react';
import { Info } from 'lucide-react';

export default function Disclaimer({ children }: { children: ReactNode }) {
  return (
    <p className="flex items-start gap-2 text-sm text-muted">
      <Info aria-hidden="true" className="mt-0.5 h-4 w-4 shrink-0" />
      <span>{children}</span>
    </p>
  );
}
