import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import LinkButton from './LinkButton';

function r(to: string) {
  render(
    <MemoryRouter>
      <LinkButton to={to}>Link</LinkButton>
    </MemoryRouter>,
  );
  return screen.getByRole('link', { name: 'Link' });
}
afterEach(cleanup);


describe('LinkButton', () => {
  it('renders an internal router link without target', () => {
    const a = r('/loans');
    expect(a).toHaveAttribute('href', '/loans');
    expect(a).not.toHaveAttribute('target');
  });

  it('opens external links safely', () => {
    const a = r('https://wa.me/919000000000');
    expect(a).toHaveAttribute('target', '_blank');
    expect(a).toHaveAttribute('rel', 'noopener noreferrer');
  });

  it('renders tel links as plain anchors', () => {
    const a = r('tel:+919000000000');
    expect(a).toHaveAttribute('href', 'tel:+919000000000');
    expect(a).not.toHaveAttribute('target');
  });
});
