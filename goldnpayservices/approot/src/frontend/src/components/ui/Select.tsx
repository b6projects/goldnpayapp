import { forwardRef, useId } from 'react';
import type { SelectHTMLAttributes } from 'react';

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  error?: string;
}

const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { label, error, id, className = '', children, ...rest },
  ref,
) {
  const generated = useId();
  const selectId = id ?? generated;
  const errorId = `${selectId}-error`;
  return (
    <div>
      <label htmlFor={selectId} className="mb-1 block text-sm font-medium text-text">
        {label}
      </label>
      <select
        ref={ref}
        id={selectId}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className={`min-h-[44px] w-full rounded-xl border border-muted bg-background px-3 py-2 text-base text-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 aria-[invalid=true]:border-red-700 ${className}`}
        {...rest}
      >
        {children}
      </select>
      {error && (
        <p id={errorId} className="mt-1 text-sm text-red-700">
          {error}
        </p>
      )}
    </div>
  );
});

export default Select;
