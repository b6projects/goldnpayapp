import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { render, screen } from '@testing-library/react';
import Button from './Button';
afterEach(cleanup);


describe('Button', () => {
  it('defaults to type button and primary styling', () => {
    render(<Button>Save</Button>);
    const btn = screen.getByRole('button', { name: 'Save' });
    expect(btn).toHaveAttribute('type', 'button');
    expect(btn.className).toContain('bg-sand');
  });

  it.each([
    ['secondary', 'border-primary'],
    ['ghost', 'bg-transparent'],
    ['whatsapp', 'bg-success'],
  ] as const)('renders the %s variant', (variant, cls) => {
    render(<Button variant={variant}>Go</Button>);
    expect(screen.getByRole('button').className).toContain(cls);
  });

  it('renders sizes', () => {
    render(<Button size="lg">Big</Button>);
    expect(screen.getByRole('button').className).toContain('text-lg');
  });

  it('is disabled and busy while loading', () => {
    render(<Button loading>Send</Button>);
    const btn = screen.getByRole('button');
    expect(btn).toBeDisabled();
    expect(btn).toHaveAttribute('aria-busy', 'true');
  });
});
