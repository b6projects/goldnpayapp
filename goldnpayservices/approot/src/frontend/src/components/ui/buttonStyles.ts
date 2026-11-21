export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'whatsapp';
export type ButtonSize = 'sm' | 'md' | 'lg';

const base =
  'inline-flex items-center justify-center gap-2 rounded-xl font-semibold transition-colors motion-reduce:transition-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60';

const variants: Record<ButtonVariant, string> = {
  primary: 'border border-accent bg-sand text-primary hover:bg-sand/80',
  secondary: 'border-2 border-primary bg-background text-primary hover:bg-surface',
  ghost: 'bg-transparent text-primary hover:bg-surface',
  // White on the WhatsApp green fails contrast, so dark text is used.
  whatsapp: 'bg-success text-text hover:bg-success/90',
};

const sizes: Record<ButtonSize, string> = {
  sm: 'min-h-[36px] px-3 py-1.5 text-sm',
  md: 'min-h-[44px] px-5 py-2.5 text-base',
  lg: 'min-h-[52px] px-6 py-3 text-lg',
};

export function buttonClasses(
  variant: ButtonVariant = 'primary',
  size: ButtonSize = 'md',
  extra = '',
): string {
  return [base, variants[variant], sizes[size], extra].filter(Boolean).join(' ');
}
