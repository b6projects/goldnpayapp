import { useLocation } from 'react-router-dom';
import { MessageCircle } from 'lucide-react';
import { site } from '../../config/site';
import { buildWhatsAppUrl } from '../../lib/whatsapp';

export default function WhatsAppFab() {
  const { pathname } = useLocation();
  if (pathname === '/consultation') return null;
  return (
    <a
      href={buildWhatsAppUrl(site.whatsapp, "Hi, I'd like a loan consultation.")}
      target="_blank"
      rel="noopener noreferrer"
      aria-label="Chat on WhatsApp"
      className="fixed bottom-6 right-4 z-40 hidden h-14 w-14 items-center justify-center rounded-full bg-success text-text shadow-lg hover:bg-success/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 md:flex"
    >
      <MessageCircle aria-hidden="true" className="h-7 w-7" />
    </a>
  );
}
