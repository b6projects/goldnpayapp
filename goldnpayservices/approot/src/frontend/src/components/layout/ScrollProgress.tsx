import { useEffect, useState } from 'react';

/** Thin gold progress bar fixed to the top of the viewport. */
export default function ScrollProgress() {
  const [pct, setPct] = useState(0);
  useEffect(() => {
    const onScroll = () => {
      const h = document.documentElement.scrollHeight - window.innerHeight;
      setPct(h > 0 ? Math.min(100, (window.scrollY / h) * 100) : 0);
    };
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);
  return (
    <div aria-hidden="true" className="fixed inset-x-0 top-0 z-50 h-1">
      <div className="h-full bg-gradient-to-r from-accent to-sand" style={{ width: `${pct}%` }} />
    </div>
  );
}
