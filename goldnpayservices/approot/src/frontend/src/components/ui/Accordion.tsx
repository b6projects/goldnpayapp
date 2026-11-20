import type { ReactNode } from 'react';
import { ChevronDown } from 'lucide-react';

export interface AccordionItem {
  id: string;
  title: string;
  content: ReactNode;
}

/** Native details/summary: keyboard operable, several items can be open at once. */
export default function Accordion({ items }: { items: AccordionItem[] }) {
  return (
    <div className="divide-y divide-surface rounded-xl border border-surface">
      {items.map((item) => (
        <details key={item.id} className="group">
          <summary className="flex min-h-[44px] cursor-pointer list-none items-center justify-between gap-4 px-4 py-3 text-base font-semibold text-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2">
            {item.title}
            <ChevronDown
              aria-hidden="true"
              className="h-5 w-5 shrink-0 transition-transform motion-reduce:transition-none group-open:rotate-180"
            />
          </summary>
          <div className="px-4 pb-4 text-base text-text">{item.content}</div>
        </details>
      ))}
    </div>
  );
}
