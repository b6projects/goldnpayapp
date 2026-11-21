import { MessageCircle, Phone } from 'lucide-react';
import { site } from '../../config/site';
import { buildWhatsAppUrl } from '../../lib/whatsapp';
import LinkButton from '../ui/LinkButton';

export default function MobileStickyBar() {
  return (
    <div className="fixed inset-x-0 bottom-0 z-40 flex gap-3 border-t border-surface bg-background px-4 pb-[max(0.75rem,env(safe-area-inset-bottom))] pt-3 md:hidden">
      <LinkButton to={`tel:${site.phoneE164}`} variant="secondary" className="flex-1">
        <Phone aria-hidden="true" className="h-5 w-5" />
        Call
      </LinkButton>
      <LinkButton
        to={buildWhatsAppUrl(site.whatsapp, "Hi, I'd like a loan consultation.")}
        variant="whatsapp"
        className="flex-1"
      >
        <MessageCircle aria-hidden="true" className="h-5 w-5" />
        WhatsApp
      </LinkButton>
    </div>
  );
}
