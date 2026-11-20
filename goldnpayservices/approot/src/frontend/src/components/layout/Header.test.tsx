import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Header from './Header';
import { loans } from '../../data/loans';
import { site } from '../../config/site';

function setup() {
  const user = userEvent.setup();
  render(
    <MemoryRouter>
      <Header />
    </MemoryRouter>,
  );
  const nav = screen.getByRole('navigation', { name: 'Primary' });
  return { user, nav };
}
afterEach(cleanup);


describe('Header', () => {
  it('shows the logo link, nav items and the consultation CTA', () => {
    const { nav } = setup();
    expect(screen.getByRole('link', { name: site.name })).toHaveAttribute('href', '/');
    for (const name of ['EMI Calculator', 'Eligibility', 'About', 'Contact']) {
      expect(within(nav).getByRole('link', { name })).toBeInTheDocument();
    }
    expect(within(nav).getByRole('link', { name: 'Free Consultation' })).toHaveAttribute(
      'href',
      '/consultation',
    );
  });

  it('Loans menu lists exactly the 7 loans plus All loans', async () => {
    const { user, nav } = setup();
    expect(loans).toHaveLength(7);
    const btn = within(nav).getByRole('button', { name: /Loans/ });
    expect(btn).toHaveAttribute('aria-expanded', 'false');
    await user.click(btn);
    const menu = document.getElementById('loans-menu') as HTMLElement;
    const links = within(menu).getAllByRole('link');
    expect(links).toHaveLength(8);
    expect(within(menu).getByRole('link', { name: 'All loans' })).toHaveAttribute('href', '/loans');
    expect(btn).toHaveAttribute('aria-expanded', 'true');
  });

  it('opens with Enter and Space', async () => {
    const { user, nav } = setup();
    const btn = within(nav).getByRole('button', { name: /Loans/ });
    btn.focus();
    await user.keyboard('{Enter}');
    expect(btn).toHaveAttribute('aria-expanded', 'true');
    await user.keyboard('{Escape}');
    expect(btn).toHaveAttribute('aria-expanded', 'false');
    await user.keyboard(' ');
    expect(btn).toHaveAttribute('aria-expanded', 'true');
  });

  it('closes on Escape from inside the menu and returns focus to the button', async () => {
    const { user, nav } = setup();
    const btn = within(nav).getByRole('button', { name: /Loans/ });
    await user.click(btn);
    await user.tab();
    await user.keyboard('{Escape}');
    expect(btn).toHaveAttribute('aria-expanded', 'false');
    expect(btn).toHaveFocus();
  });

  it('closes on outside click', async () => {
    const { user, nav } = setup();
    const btn = within(nav).getByRole('button', { name: /Loans/ });
    await user.click(btn);
    await user.click(document.body);
    expect(btn).toHaveAttribute('aria-expanded', 'false');
  });
});
