import { useId, useRef, useState } from 'react';
import type { KeyboardEvent, ReactNode } from 'react';

export interface TabItem {
  id: string;
  label: string;
  content: ReactNode;
}

/** Accessible tabs: roving tabindex, Arrow/Home/End keys, automatic activation. */
export default function Tabs({ tabs, label }: { tabs: TabItem[]; label: string }) {
  const base = useId();
  const [active, setActive] = useState(0);
  const refs = useRef<(HTMLButtonElement | null)[]>([]);

  function select(index: number) {
    setActive(index);
    refs.current[index]?.focus();
  }

  function onKeyDown(e: KeyboardEvent<HTMLButtonElement>) {
    const last = tabs.length - 1;
    if (e.key === 'ArrowRight') {
      e.preventDefault();
      select(active === last ? 0 : active + 1);
    } else if (e.key === 'ArrowLeft') {
      e.preventDefault();
      select(active === 0 ? last : active - 1);
    } else if (e.key === 'Home') {
      e.preventDefault();
      select(0);
    } else if (e.key === 'End') {
      e.preventDefault();
      select(last);
    }
  }

  return (
    <div>
      <div role="tablist" aria-label={label} className="flex gap-2 border-b border-surface">
        {tabs.map((tab, i) => (
          <button
            key={tab.id}
            ref={(el) => {
              refs.current[i] = el;
            }}
            type="button"
            role="tab"
            id={`${base}-tab-${tab.id}`}
            aria-selected={i === active}
            aria-controls={`${base}-panel-${tab.id}`}
            tabIndex={i === active ? 0 : -1}
            onClick={() => setActive(i)}
            onKeyDown={onKeyDown}
            className={`min-h-[44px] px-4 py-2 text-base font-semibold focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 ${
              i === active ? 'border-b-2 border-primary text-primary' : 'text-muted'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>
      {tabs.map((tab, i) => (
        <div
          key={tab.id}
          role="tabpanel"
          id={`${base}-panel-${tab.id}`}
          aria-labelledby={`${base}-tab-${tab.id}`}
          hidden={i !== active}
          className="pt-4"
        >
          {i === active ? tab.content : null}
        </div>
      ))}
    </div>
  );
}
