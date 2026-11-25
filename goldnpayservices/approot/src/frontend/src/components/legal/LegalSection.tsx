import type { ReactNode } from 'react';

export function LegalSection({ id, title, children }: { id: string; title: string; children: ReactNode }) {
  return (
    <section aria-labelledby={id} className="space-y-2">
      <h2 id={id} className="text-xl font-semibold">
        {title}
      </h2>
      {children}
    </section>
  );
}

export function Bullets({ items }: { items: ReactNode[] }) {
  return (
    <ul className="list-disc space-y-1 pl-6">
      {items.map((item, i) => (
        <li key={i}>{item}</li>
      ))}
    </ul>
  );
}

export function Numbered({ items }: { items: ReactNode[] }) {
  return (
    <ol className="list-decimal space-y-1 pl-6">
      {items.map((item, i) => (
        <li key={i}>{item}</li>
      ))}
    </ol>
  );
}

export function LastUpdated({ date }: { date: string }) {
  return <p className="text-sm italic">Last updated: {date}</p>;
}

export function Tel({ phone }: { phone: string }) {
  return (
    <a className="underline" href={`tel:${phone.replace(/\s/g, '')}`}>
      {phone}
    </a>
  );
}

export function Mail({ email }: { email: string }) {
  return (
    <a className="underline" href={`mailto:${email}`}>
      {email}
    </a>
  );
}
