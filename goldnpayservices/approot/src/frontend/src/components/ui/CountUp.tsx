import { useEffect, useState } from 'react';
import { useInView } from '../../hooks/useInView';

const reduced = () =>
  typeof window !== 'undefined' &&
  typeof window.matchMedia === 'function' &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches;

const canAnimate = () =>
  typeof window !== 'undefined' && 'IntersectionObserver' in window && !reduced();

/**
 * Animates the first number found in `value` (e.g. "20+" -> 0..20 then "+").
 * The final text is rendered on the server and on the first client render, so crawlers and
 * no-JS visitors see the real figure; the count-up starts only after mounting.
 * Non-numeric values and reduced-motion users keep the final text.
 */
export default function CountUp({ value, duration = 1600 }: { value: string; duration?: number }) {
  const match = /^(\D*)(\d[\d,]*)(.*)$/.exec(value);
  const { ref, inView } = useInView<HTMLSpanElement>(0.5);
  const target = match ? Number(match[2]!.replace(/,/g, '')) : 0;
  const [animate, setAnimate] = useState(false);
  const [n, setN] = useState(0);

  useEffect(() => setAnimate(canAnimate()), []);

  useEffect(() => {
    if (!match || !animate) return;
    if (!inView) {
      setN(0);
      return;
    }
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
  }, [animate, inView, target, duration]);

  if (!match) return <span>{value}</span>;
  const text = animate ? n.toLocaleString('en-IN') : match[2]!;
  return (
    <span ref={ref}>
      {match[1]}
      {text}
      {match[3]}
    </span>
  );
}
