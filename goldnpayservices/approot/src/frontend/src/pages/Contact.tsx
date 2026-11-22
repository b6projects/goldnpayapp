import type { ReactNode } from 'react';
import { Phone, MessageCircle, MapPin } from 'lucide-react';
import Section from '../components/ui/Section';
import Card from '../components/ui/Card';
import ConsultationForm from '../components/forms/ConsultationForm';
import { site } from '../config/site';
import { buildWhatsAppUrl } from '../lib/whatsapp';
import Seo from '../components/seo/Seo';
import { pageSeo } from '../seo/pageSeo';

function ContactCard({ icon, title, children }: { icon: ReactNode; title: string; children: ReactNode }) {
  return (
    <Card className="space-y-2">
      <Seo {...pageSeo('/contact')} />
      <h2 className="flex items-center gap-2 text-lg font-semibold text-text">
        {icon}
        {title}
      </h2>
      <div className="text-base text-text">{children}</div>
    </Card>
  );
}

const linkClass = 'inline-flex min-h-[44px] items-center underline';

export default function Contact() {
  return (
    <Section>
      <h1 className="text-3xl font-bold text-text md:text-4xl">Contact Us</h1>
      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <ContactCard icon={<Phone aria-hidden="true" className="h-5 w-5" />} title="Call">
          <a className={linkClass} href={`tel:${site.phoneE164}`}>
            {site.phone}
          </a>
        </ContactCard>
        <ContactCard icon={<MessageCircle aria-hidden="true" className="h-5 w-5" />} title="WhatsApp">
          <a
            className={linkClass}
            href={buildWhatsAppUrl(site.whatsapp, "Hi, I'd like a loan consultation.")}
            target="_blank"
            rel="noopener noreferrer"
          >
            Chat on WhatsApp
          </a>
        </ContactCard>
        <ContactCard icon={<MapPin aria-hidden="true" className="h-5 w-5" />} title="Visit">
          <p>{site.address}</p>
        </ContactCard>
      </div>
      <section aria-labelledby="contact-hours" className="mt-8">
        <h2 id="contact-hours" className="text-xl font-semibold text-text">Business hours</h2>
        <p className="mt-2 text-base text-text">{site.hours}</p>
      </section>
      {site.mapEmbedUrl !== '' && (
        <iframe
          title="Map showing our office location"
          src={site.mapEmbedUrl}
          loading="lazy"
          referrerPolicy="no-referrer"
          className="mt-8 h-80 w-full rounded-xl border border-surface"
        />
      )}
      <div className="mt-8 max-w-xl">
        <ConsultationForm />
      </div>
    </Section>
  );
}
