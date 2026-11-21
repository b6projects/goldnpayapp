import { useEffect, useRef, useState } from 'react';

/** True when IntersectionObserver is unavailable (old browsers, jsdom) so content is never hidden. */
const NO_OBSERVER = typeof window === 'undefined' || !('IntersectionObserver' in window);

/** Fires once when the element first scrolls into view. */
export function useInView<T extends HTMLElement>(threshold = 0.15) {
  const ref = useRef<T>(null);
  const [inView, setInView] = useState(NO_OBSERVER);

  useEffect(() => {
    const el = ref.current;
    if (!el || NO_OBSERVER) return;
    const io = new IntersectionObserver(
      ([entry]) => {
        if (entry?.isIntersecting) {
          setInView(true);
          io.disconnect();
        }
      },
      { threshold, rootMargin: '0px 0px -40px 0px' },
    );
    io.observe(el);
    return () => io.disconnect();
  }, [threshold]);

  return { ref, inView };
}
