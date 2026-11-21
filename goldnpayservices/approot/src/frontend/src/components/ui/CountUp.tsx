import { useEffect, useState } from 'react';
import { useInView } from '../../hooks/useInView';

const reduced = () =>
  typeof window !== 'undefined' &&
  typeof window.matchMedia === 'function' &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches;

/**
 * Animates the first number found in `value` (e.g. "20+" -> 0..20 then "+").
 * Non-numeric values and reduced-motion users get the final text immediately.
 */
export default function CountUp({ value, duration = 1600 }: { value: string; duration?: number }) {
  const match = /^(\D*)(\d[\d,]*)(.*)$/.exec(value);
  const { ref, inView } = useInView<HTMLSpanElement>(0.5);
  const target = match ? Number(match[2]!.replace(/,/g, '')) : 0;
  const [n, setN] = useState<number | null>(null);

  useEffect(() => {
    if (!match || !inView || typeof IntersectionObserver === 'undefined' || reduced()) return;
    let raf = 0;
    const start = performance.now();
    const tick = (now: number) => {
      const t = Math.min((now - start) / duration, 1);
      setN(Math.round(target * (1 - Math.pow(1 - t, 3))));
      if (t < 1) raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inView, target, duration]);

  if (!match) return <span>{value}</span>;
  // Static text when animation is impossible or unwanted; otherwise count from 0 once in view.
  const animate = typeof IntersectionObserver !== 'undefined' && !reduced();
  const text = animate ? (n ?? 0).toLocaleString('en-IN') : match[2]!;
  return (
    <span ref={ref}>
      {match[1]}
      {text}
      {match[3]}
    </span>
  );
}
