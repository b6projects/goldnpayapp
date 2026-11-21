import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import Tabs from './Tabs';

afterEach(cleanup);

function r() {
  render(
    <Tabs
      label="Docs"
      tabs={[
        { id: 'a', label: 'Salaried', content: <p>salaried docs</p> },
        { id: 'b', label: 'Self-employed', content: <p>self docs</p> },
      ]}
    />,
  );
}

describe('Tabs', () => {
  it('shows the first tab by default with correct aria attributes', () => {
    r();
    const [first, second] = screen.getAllByRole('tab');
    expect(first).toHaveAttribute('aria-selected', 'true');
    expect(second).toHaveAttribute('aria-selected', 'false');
    expect(first).toHaveAttribute('tabindex', '0');
    expect(second).toHaveAttribute('tabindex', '-1');
    expect(screen.getByRole('tabpanel')).toHaveTextContent('salaried docs');
    expect(first.getAttribute('aria-controls')).toBe(screen.getByRole('tabpanel').id);
  });

  it('switches on click', () => {
    r();
    fireEvent.click(screen.getByRole('tab', { name: 'Self-employed' }));
    expect(screen.getByRole('tabpanel')).toHaveTextContent('self docs');
  });

  it('switches with arrow keys, Home and End', () => {
    r();
    const first = screen.getByRole('tab', { name: 'Salaried' });
    fireEvent.keyDown(first, { key: 'ArrowRight' });
    expect(screen.getByRole('tab', { name: 'Self-employed' })).toHaveAttribute('aria-selected', 'true');
    fireEvent.keyDown(screen.getByRole('tab', { name: 'Self-employed' }), { key: 'Home' });
    expect(first).toHaveAttribute('aria-selected', 'true');
    fireEvent.keyDown(first, { key: 'End' });
    expect(screen.getByRole('tab', { name: 'Self-employed' })).toHaveAttribute('aria-selected', 'true');
  });
});
