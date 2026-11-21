import type { HTMLAttributes } from 'react';

export default function Badge({ className = '', ...rest }: HTMLAttributes<HTMLSpanElement>) {
  return (
    <span
      className={`inline-flex items-center rounded-full bg-surface px-3 py-1 text-sm font-medium text-primary ${className}`}
      {...rest}
    />
  );
}
