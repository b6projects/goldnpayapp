import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Footer from './Footer';
import { site } from '../../config/site';

function r() {
  render(
    <MemoryRouter>
      <Footer />
    </MemoryRouter>,
  );
  return screen.getByRole('contentinfo');
}
afterEach(cleanup);


describe('Footer', () => {
  it('shows the exact compliance statement', () => {
    const footer = r();
    const text = `${site.name} is a loan consultant and not a lender. Loans are sanctioned at the sole discretion of our partner banks and NBFCs. Interest rates shown are indicative and subject to the lender's policy and your credit profile.`;
    expect(footer.textContent?.replace(/\s+/g, ' ')).toContain(text);
  });

  it('has legal links and the copyright line', () => {
    const footer = r();
    expect(within(footer).getByRole('link', { name: 'Privacy' })).toHaveAttribute('href', '/privacy');
    expect(within(footer).getByRole('link', { name: 'Terms' })).toHaveAttribute('href', '/terms');
    expect(within(footer).getByRole('link', { name: 'Disclaimer' })).toHaveAttribute('href', '/disclaimer');
    expect(within(footer).getByRole('link', { name: 'Partners' })).toHaveAttribute('href', '/partners');
    expect(footer.textContent).toContain(`© ${new Date().getFullYear()} ${site.name}`);
  });

  it('has four column headings', () => {
    const footer = r();
    expect(within(footer).getAllByRole('heading', { level: 2 })).toHaveLength(4);
  });

  it('does not render empty social links', () => {
    const footer = r();
    expect(within(footer).queryByRole('link', { name: 'LinkedIn' })).not.toBeInTheDocument();
    expect(within(footer).queryByRole('link', { name: 'Instagram' })).not.toBeInTheDocument();
    expect(within(footer).queryByRole('link', { name: 'Facebook' })).not.toBeInTheDocument();
  });
});
