import type { HTMLAttributes } from 'react';

export default function Section({ className = '', children, ...rest }: HTMLAttributes<HTMLElement>) {
  return (
    <section className={`py-12 md:py-16 ${className}`} {...rest}>
      <div className="mx-auto max-w-7xl px-4">{children}</div>
    </section>
  );
}
