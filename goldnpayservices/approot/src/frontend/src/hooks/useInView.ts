import { useEffect, useRef, useState } from 'react';

const hasObserver = () => typeof window !== 'undefined' && 'IntersectionObserver' in window;

/**
 * Fires once when the element first scrolls into view.
 *
 * Starts as "in view" so the server-rendered HTML (and crawlers, old browsers, jsdom) always
 * sees the content and the first client render matches it. After mounting, elements that are
 * below the fold are hidden again and revealed when they scroll into view.
 */
export function useInView<T extends HTMLElement>(threshold = 0.15) {
  const ref = useRef<T>(null);
  const [inView, setInView] = useState(true);

  useEffect(() => {
    const el = ref.current;
    if (!el || !hasObserver()) return;
    if (el.getBoundingClientRect().top < window.innerHeight) return; // already on screen
    setInView(false);
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
