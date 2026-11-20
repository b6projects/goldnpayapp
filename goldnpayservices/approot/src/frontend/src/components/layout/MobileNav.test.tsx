import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Header from './Header';

function setup() {
  const user = userEvent.setup();
  const utils = render(
    <MemoryRouter initialEntries={['/']}>
      <Header />
    </MemoryRouter>,
  );
  return { user, ...utils };
}

const hamburger = () => screen.getByRole('button', { name: 'Open menu' });
afterEach(cleanup);


describe('MobileNav', () => {
  it('is closed by default', () => {
    setup();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(hamburger()).toHaveAttribute('aria-expanded', 'false');
  });

  it('opens via the hamburger and closes via the close button', async () => {
    const { user } = setup();
    await user.click(hamburger());
    expect(screen.getByRole('dialog', { name: 'Main menu' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Close menu' }));
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('closes on Escape', async () => {
    const { user } = setup();
    await user.click(hamburger());
    await user.keyboard('{Escape}');
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('closes on backdrop click', async () => {
    const { user } = setup();
    await user.click(hamburger());
    await user.click(screen.getByTestId('mobile-nav-backdrop'));
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('closes on route change', async () => {
    const { user } = setup();
    await user.click(hamburger());
    const dialog = screen.getByRole('dialog');
    await user.click(within(dialog).getByRole('link', { name: 'About' }));
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('locks body scroll while open and restores it on close', async () => {
    const { user } = setup();
    await user.click(hamburger());
    expect(document.body.style.overflow).toBe('hidden');
    await user.keyboard('{Escape}');
    expect(document.body.style.overflow).toBe('');
  });

  it('restores body scroll on unmount', async () => {
    const { user, unmount } = setup();
    await user.click(hamburger());
    unmount();
    expect(document.body.style.overflow).toBe('');
  });

  it('moves focus in, wraps Tab and Shift+Tab, and returns focus to the hamburger', async () => {
    const { user } = setup();
    await user.click(hamburger());
    const closeBtn = screen.getByRole('button', { name: 'Close menu' });
    expect(closeBtn).toHaveFocus();

    const dialog = screen.getByRole('dialog');
    const links = within(dialog).getAllByRole('link');
    const last = links[links.length - 1];

    await user.tab({ shift: true });
    expect(last).toHaveFocus();
    await user.tab();
    expect(closeBtn).toHaveFocus();

    await user.keyboard('{Escape}');
    expect(hamburger()).toHaveFocus();
  });
});
