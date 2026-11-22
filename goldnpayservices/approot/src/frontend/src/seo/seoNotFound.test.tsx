import { describe, expect, it } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { routes } from '../routes';

describe('document head for an unknown page', () => {
  it('is noindex and has no canonical link', async () => {
    render(<RouterProvider router={createMemoryRouter(routes, { initialEntries: ['/does-not-exist'] })} />);
    await screen.findByRole('heading', { level: 1, name: 'Page Not Found' });
    await waitFor(() =>
      expect(document.querySelector('meta[name="robots"]')?.getAttribute('content')).toBe('noindex, follow'),
    );
    expect(document.querySelector('link[rel="canonical"]')).toBeNull();
  });
});
