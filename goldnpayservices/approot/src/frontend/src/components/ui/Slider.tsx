import { useId } from 'react';
import type { InputHTMLAttributes } from 'react';

export interface SliderProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type' | 'value' | 'onChange'> {
  label: string;
  value: number;
  min: number;
  max: number;
  step?: number;
  onChange: (value: number) => void;
  valueText?: string;
}

export default function Slider({
  label,
  value,
  min,
  max,
  step = 1,
  onChange,
  valueText,
  id,
  className = '',
  ...rest
}: SliderProps) {
  const generated = useId();
  const inputId = id ?? generated;
  return (
    <div>
      <label htmlFor={inputId} className="mb-1 block text-sm font-medium text-text">
        {label}
      </label>
      <input
        id={inputId}
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        aria-valuetext={valueText ?? String(value)}
        onChange={(e) => onChange(Number(e.target.value))}
        className={`h-2 w-full cursor-pointer accent-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 ${className}`}
        {...rest}
      />
    </div>
  );
}
