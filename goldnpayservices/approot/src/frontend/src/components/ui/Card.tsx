import type { HTMLAttributes } from 'react';

export default function Card({ className = '', ...rest }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={`rounded-xl border border-surface bg-background p-6 shadow-sm ${className}`}
      {...rest}
    />
  );
}
