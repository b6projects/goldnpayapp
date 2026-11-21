import { useId, useState } from 'react';
import Slider from '../ui/Slider';

export interface SliderFieldProps {
  /** Visible label of the slider, e.g. "Loan amount". */
  label: string;
  /** Short noun used in the clamp message, e.g. "Amount". */
  name: string;
  value: number;
  min: number;
  max: number;
  step: number;
  /** Decimal places kept on every change (0 for integers). */
  decimals?: number;
  onChange: (value: number) => void;
  /** Formats a value for aria-valuetext and the clamp message. */
  format: (value: number) => string;
  /** Unit hint shown beside the number field, e.g. "months". */
  unitLabel?: string;
}

function round(value: number, decimals: number): number {
  const f = 10 ** decimals;
  return Math.round(value * f) / f;
}

/** A slider paired with a number field; both stay in sync and out-of-range entries are clamped. */
export default function SliderField({
  label,
  name,
  value,
  min,
  max,
  step,
  decimals = 0,
  onChange,
  format,
  unitLabel,
}: SliderFieldProps) {
  const messageId = useId();
  const [draft, setDraft] = useState<string | null>(null);
  const [message, setMessage] = useState('');

  const commit = (next: number) => {
    setMessage('');
    onChange(round(next, decimals));
  };

  const handleText = (text: string) => {
    setDraft(text);
    const n = Number(text);
    if (text.trim() !== '' && Number.isFinite(n) && n >= min && n <= max) {
      commit(n);
    }
  };

  const finalise = () => {
    if (draft === null) return;
    const n = Number(draft);
    if (draft.trim() === '' || !Number.isFinite(n)) {
      setDraft(null);
      return;
    }
    const clamped = Math.min(max, Math.max(min, round(n, decimals)));
    if (clamped !== round(n, decimals)) {
      onChange(clamped);
      setMessage(`${name} adjusted to the allowed range: ${format(min)} to ${format(max)}`);
    } else {
      commit(n);
    }
    setDraft(null);
  };

  return (
    <div className="space-y-1">
      <Slider
        label={label}
        value={value}
        min={min}
        max={max}
        step={step}
        valueText={format(value)}
        onChange={(v) => {
          setDraft(null);
          commit(v);
        }}
      />
      <div className="flex flex-wrap items-center gap-x-2">
        <input
          type="text"
          inputMode="decimal"
          aria-label={`${label} value`}
          aria-describedby={messageId}
          value={draft ?? String(value)}
          onChange={(e) => handleText(e.target.value)}
          onBlur={finalise}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault();
              finalise();
            }
          }}
          className="min-h-[44px] w-32 rounded-xl border border-muted bg-background px-3 py-2 text-base text-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        />
        {unitLabel && <span className="text-sm text-muted">{unitLabel}</span>}
        <span className="text-sm text-muted">
          {format(min)} to {format(max)}
        </span>
      </div>
      <p id={messageId} aria-live="polite" className="text-sm text-red-700">
        {message}
      </p>
    </div>
  );
}
