import { MessageCircle } from 'lucide-react';
import Section from '../components/ui/Section';
import Card from '../components/ui/Card';
import { site } from '../config/site';
import { buildWhatsAppUrl } from '../lib/whatsapp';
import Seo from '../components/seo/Seo';
import { pageSeo } from '../seo/pageSeo';

export default function Consultation() {
  return (
    <Section>
      <Seo {...pageSeo('/consultation')} />
      <h1 className="text-3xl font-bold text-text md:text-4xl">Get Free Consultation</h1>
      <Card className="mt-6 max-w-xl space-y-3">
        <h2 className="flex items-center gap-2 text-lg font-semibold text-text">
          <MessageCircle aria-hidden="true" className="h-5 w-5" />
          Reach us on WhatsApp
        </h2>
        <p className="text-base text-text">
          Message us on WhatsApp at{' '}
          <a
            className="inline-flex min-h-[44px] items-center font-semibold underline"
            href={buildWhatsAppUrl(site.whatsapp, "Hi, I'd like a loan consultation.")}
            target="_blank"
            rel="noopener noreferrer"
          >
            {site.phone}
          </a>{' '}
          and our team will get back to you during business hours ({site.hours}).
        </p>
      </Card>
    </Section>
  );
}
