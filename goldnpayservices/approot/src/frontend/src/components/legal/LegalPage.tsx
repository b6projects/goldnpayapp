import type { ReactNode } from 'react';
import Section from '../ui/Section';
import { site } from '../../config/site';

export interface LegalPageProps {
  title: string;
  children: ReactNode;
  showTemplateBanner?: boolean;
}

export default function LegalPage({
  title,
  children,
  showTemplateBanner = !site.legalReviewed,
}: LegalPageProps) {
  return (
    <Section>
      <div className="mx-auto max-w-3xl">
        <h1 className="text-3xl font-bold text-text md:text-4xl">{title}</h1>
        {showTemplateBanner && (
          <div role="note" className="mt-4 rounded-xl border border-muted bg-surface p-4 text-base text-text">
            Template — to be reviewed by a legal professional before launch.
          </div>
        )}
        <div className="mt-6 space-y-6 text-base text-text">{children}</div>
      </div>
    </Section>
  );
}
