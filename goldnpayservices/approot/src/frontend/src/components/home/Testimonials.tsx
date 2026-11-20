import Section from '../ui/Section';
import Card from '../ui/Card';
import Badge from '../ui/Badge';
import { site } from '../../config/site';
import { testimonials } from '../../data/testimonials';
import type { Testimonial } from '../../types';

export interface TestimonialsProps {
  items?: Testimonial[];
  showSample?: boolean;
}

export default function Testimonials({
  items = testimonials,
  showSample = site.showSampleTestimonials,
}: TestimonialsProps) {
  if (!showSample && items.length === 0) return null;
  return (
    <Section className="bg-surface" aria-labelledby="testimonials-heading">
      <div className="mb-8 flex flex-wrap items-center gap-3">
        <h2 id="testimonials-heading" className="text-2xl font-bold text-text md:text-3xl">
          What customers say
        </h2>
        {showSample && <Badge>Sample</Badge>}
      </div>
      <ul className="grid grid-cols-1 gap-6 md:grid-cols-3">
        {items.map((t) => (
          <li key={t.name}>
            <Card className="h-full">
              <p className="text-base text-text">&ldquo;{t.quote}&rdquo;</p>
              <p className="mt-4 text-sm font-semibold text-text">{t.name}</p>
              <p className="text-sm text-muted">
                {t.city} &middot; {t.loanType}
              </p>
              <p className="mt-1 text-sm text-muted">Rating: {t.rating} out of 5</p>
            </Card>
          </li>
        ))}
      </ul>
    </Section>
  );
}
