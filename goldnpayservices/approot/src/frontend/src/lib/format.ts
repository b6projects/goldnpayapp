/** Display formatters for INR amounts and tenures. */

function groupIndian(n: number): string {
  const s = String(Math.round(Math.abs(n)));
  if (s.length <= 3) return s;
  const last3 = s.slice(-3);
  const rest = s.slice(0, -3).replace(/\B(?=(\d{2})+(?!\d))/g, ',');
  return `${rest},${last3}`;
}

/** e.g. 1000000 -> "₹10,00,000". */
export function formatINR(amount: number): string {
  const rounded = Math.round(amount);
  const sign = rounded < 0 ? '-' : '';
  return `${sign}₹${groupIndian(rounded)}`;
}

function trim(n: number): string {
  return String(Number(n.toFixed(2)));
}

/** e.g. 1000000 -> "₹10 L", 15000000 -> "₹1.5 Cr", 50000 -> "₹50 K". */
export function formatINRCompact(amount: number): string {
  const abs = Math.abs(amount);
  const sign = amount < 0 ? '-' : '';
  if (abs >= 1e7) return `${sign}₹${trim(abs / 1e7)} Cr`;
  if (abs >= 1e5) return `${sign}₹${trim(abs / 1e5)} L`;
  if (abs >= 1e3) return `${sign}₹${trim(abs / 1e3)} K`;
  return formatINR(amount);
}

function plural(n: number, unit: string): string {
  return `${n} ${unit}${n === 1 ? '' : 's'}`;
}

/** e.g. 60 -> "5 years", 8 -> "8 months", 18 -> "1 year 6 months". */
export function formatTenure(months: number): string {
  const years = Math.floor(months / 12);
  const rem = months % 12;
  if (years === 0) return plural(rem, 'month');
  if (rem === 0) return plural(years, 'year');
  return `${plural(years, 'year')} ${plural(rem, 'month')}`;
}
