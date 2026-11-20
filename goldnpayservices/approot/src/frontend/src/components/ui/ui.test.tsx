import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { render, screen } from '@testing-library/react';
import Card from './Card';
import Section from './Section';
import Badge from './Badge';
import Disclaimer from './Disclaimer';
afterEach(cleanup);


describe('presentational primitives', () => {
  it('Card renders children with rounded-xl', () => {
    render(<Card data-testid="c">Body</Card>);
    expect(screen.getByTestId('c').className).toContain('rounded-xl');
    expect(screen.getByText('Body')).toBeInTheDocument();
  });

  it('Section applies spacing and max width container', () => {
    render(<Section data-testid="s">Inside</Section>);
    expect(screen.getByTestId('s').className).toContain('py-12 md:py-16');
    expect(screen.getByText('Inside').className).toContain('max-w-7xl');
  });

  it('Badge renders text', () => {
    render(<Badge>New</Badge>);
    expect(screen.getByText('New')).toBeInTheDocument();
  });

  it('Disclaimer renders text with a hidden icon', () => {
    const { container } = render(<Disclaimer>Indicative only</Disclaimer>);
    expect(screen.getByText('Indicative only')).toBeInTheDocument();
    expect(container.querySelector('svg')).toHaveAttribute('aria-hidden', 'true');
  });
});
