import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Contact from './Contact';
import { site } from '../config/site';

afterEach(cleanup);

function renderContact() {
  return render(
    <MemoryRouter>
      <Contact />
    </MemoryRouter>,
  );
}

describe('Contact page', () => {
  it('renders four cards with correct links', () => {
    renderContact();
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    for (const t of ['Call', 'WhatsApp', 'Email', 'Visit']) {
      expect(screen.getByRole('heading', { level: 2, name: t })).toBeInTheDocument();
    }
    expect(screen.getByRole('link', { name: site.phone })).toHaveAttribute('href', `tel:${site.phoneE164}`);
    expect(screen.getByRole('link', { name: site.email })).toHaveAttribute('href', `mailto:${site.email}`);
    const wa = screen.getByRole('link', { name: 'Chat on WhatsApp' });
    expect(wa.getAttribute('href')).toContain(`https://wa.me/${site.whatsapp}`);
    expect(wa).toHaveAttribute('target', '_blank');
    expect(wa).toHaveAttribute('rel', 'noopener noreferrer');
  });

  it('shows hours, no map while mapEmbedUrl is empty, and the placeholder form', () => {
    const { container } = renderContact();
    expect(screen.getByText(site.hours)).toBeInTheDocument();
    expect(container.querySelector('iframe')).toBeNull();
    expect(screen.getByRole('link', { name: 'Request a consultation' })).toHaveAttribute(
      'href',
      '/consultation',
    );
  });
});
