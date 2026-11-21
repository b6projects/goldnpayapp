import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import About from './About';

afterEach(cleanup);

describe('About page', () => {
  it('renders h1, mission, exactly three values and the how-we-are-paid section', () => {
    render(
      <MemoryRouter>
        <About />
      </MemoryRouter>,
    );
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    expect(screen.getByRole('heading', { level: 2, name: 'Our mission' })).toBeInTheDocument();
    expect(screen.getAllByRole('heading', { level: 3 })).toHaveLength(3);
    expect(screen.getByRole('heading', { level: 2, name: "How we're paid" })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Contact us' })).toHaveAttribute('href', '/contact');
  });
});
