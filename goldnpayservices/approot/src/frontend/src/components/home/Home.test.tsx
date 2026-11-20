import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Home from '../../pages/Home';
import { site } from '../../config/site';
import { stats } from '../../data/stats';
import { faqs } from '../../data/faqs';
import { howItWorksSteps, whyUsPoints } from '../../data/home';

afterEach(cleanup);

function r() {
  render(
    <MemoryRouter>
      <Home />
    </MemoryRouter>,
  );
}

function region(name: string) {
  return screen.getByRole('region', { name });
}

describe('Home', () => {
  it('has exactly one h1 equal to the tagline', () => {
    r();
    const h1s = screen.getAllByRole('heading', { level: 1 });
    expect(h1s).toHaveLength(1);
    expect(h1s[0]).toHaveTextContent(site.tagline);
  });

  it('renders the 9 sections in the specified order', () => {
    r();
    const names = screen.getAllByRole('region').map((el) => el.getAttribute('aria-label') ?? el.querySelector('h2')?.textContent);
    expect(names).toEqual([
      'Introduction',
      'Our numbers',
      'Our loan products',
      'How it works',
      'Why choose us',
      'Our lending partners',
      'What customers say',
      'Frequently asked questions',
      'Not sure which loan is right for you?',
    ]);
  });

  it('shows the stats', () => {
    r();
    const sec = region('Our numbers');
    for (const s of stats) {
      expect(within(sec).getByText(s.label)).toBeInTheDocument();
      expect(within(sec).getByText(s.value)).toBeInTheDocument();
    }
    expect(stats).toHaveLength(4);
  });

  it('shows the 4 how-it-works steps in order', () => {
    r();
    const items = within(region('How it works')).getAllByRole('listitem');
    expect(items).toHaveLength(4);
    howItWorksSteps.forEach((s, i) => expect(items[i]).toHaveTextContent(s.title));
  });

  it('shows the 4 why-us points', () => {
    r();
    const items = within(region('Why choose us')).getAllByRole('listitem');
    expect(items).toHaveLength(4);
    whyUsPoints.forEach((p, i) => expect(items[i]).toHaveTextContent(p.title));
  });

  it('links the partners caption to /partners', () => {
    r();
    const sec = region('Our lending partners');
    expect(sec).toHaveTextContent('Indicative list. See all partners');
    expect(within(sec).getByRole('link', { name: 'See all partners' })).toHaveAttribute('href', '/partners');
  });

  it('shows the first 5 FAQs and a link to all', () => {
    r();
    const sec = region('Frequently asked questions');
    faqs.slice(0, 5).forEach((f) => expect(within(sec).getByText(f.q)).toBeInTheDocument());
    expect(within(sec).queryByText(faqs[5].q)).not.toBeInTheDocument();
    expect(within(sec).getByRole('link', { name: 'View all FAQs' })).toHaveAttribute('href', '/faq');
  });

  it('has the closing CTA band', () => {
    r();
    const sec = region('Not sure which loan is right for you?');
    expect(within(sec).getByRole('link', { name: 'Get Free Consultation' })).toHaveAttribute('href', '/consultation');
  });

  it('never claims guaranteed outcomes', () => {
    r();
    expect(document.body.textContent?.toLowerCase()).not.toContain('guaranteed');
  });
});
