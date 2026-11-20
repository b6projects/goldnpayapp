import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import WhatsAppFab from './WhatsAppFab';
import MobileStickyBar from './MobileStickyBar';
import { site } from '../../config/site';

function fab(path: string) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <WhatsAppFab />
    </MemoryRouter>,
  );
}
afterEach(cleanup);


describe('WhatsAppFab', () => {
  it('has an accessible name and an encoded wa.me link', () => {
    fab('/');
    const a = screen.getByRole('link', { name: 'Chat on WhatsApp' });
    expect(a).toHaveAttribute(
      'href',
      `https://wa.me/${site.whatsapp}?text=${encodeURIComponent("Hi, I'd like a loan consultation.")}`,
    );
    expect(a).toHaveAttribute('rel', 'noopener noreferrer');
    expect(a).toHaveAttribute('target', '_blank');
  });

  it('is hidden below md', () => {
    fab('/');
    const cls = screen.getByRole('link').className;
    expect(cls).toContain('hidden');
    expect(cls).toContain('md:flex');
  });

  it('is absent on /consultation', () => {
    fab('/consultation');
    expect(screen.queryByRole('link', { name: 'Chat on WhatsApp' })).not.toBeInTheDocument();
  });
});

describe('MobileStickyBar', () => {
  it('has Call and WhatsApp links and is hidden from md up', () => {
    const { container } = render(
      <MemoryRouter initialEntries={['/consultation']}>
        <MobileStickyBar />
      </MemoryRouter>,
    );
    expect(screen.getByRole('link', { name: 'Call' })).toHaveAttribute('href', `tel:${site.phoneE164}`);
    expect(screen.getByRole('link', { name: 'WhatsApp' }).getAttribute('href')).toContain('https://wa.me/');
    expect((container.firstChild as HTMLElement).className).toContain('md:hidden');
  });
});
