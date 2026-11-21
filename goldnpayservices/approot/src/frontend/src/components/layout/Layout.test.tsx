import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { routes } from '../../routes';
import { site } from '../../config/site';

function renderAt(path: string) {
  const router = createMemoryRouter(routes, { initialEntries: [path] });
  render(<RouterProvider router={router} />);
}
afterEach(cleanup);


describe('Layout', () => {
  it('has landmarks and main#main', async () => {
    renderAt('/');
    await screen.findByRole('heading', { level: 1 });
    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(screen.getByRole('contentinfo')).toBeInTheDocument();
    const main = screen.getByRole('main');
    expect(main).toHaveAttribute('id', 'main');
    expect(main).toHaveAttribute('tabindex', '-1');
  });

  it('skip link is the first tabbable element and targets #main', async () => {
    const user = userEvent.setup();
    renderAt('/');
    await screen.findByRole('heading', { level: 1 });
    await user.tab();
    const skip = screen.getByRole('link', { name: 'Skip to main content' });
    expect(skip).toHaveFocus();
    expect(skip).toHaveAttribute('href', '#main');
  });

  it.each(['/', '/loans', '/contact', '/no-such-page'])(
    'shows the not-a-lender statement on %s',
    async (path) => {
      renderAt(path);
      await screen.findByRole('heading', { level: 1 });
      const footer = screen.getByRole('contentinfo');
      expect(footer.textContent).toContain(`${site.name} is a loan consultant and not a lender.`);
    },
  );
});
