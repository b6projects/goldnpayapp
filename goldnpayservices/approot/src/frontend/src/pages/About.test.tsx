import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import About from './About';
import { loans } from '../data/loans';

afterEach(cleanup);

function renderAbout() {
  render(
    <MemoryRouter>
      <About />
    </MemoryRouter>,
  );
}

describe('About page', () => {
  it('renders one h1 and the DSA description sections', () => {
    renderAbout();
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    for (const name of [
      'Who we are',
      'What is a loan DSA?',
      'What we do for you',
      'How it works',
      'Loans we help with',
      'What you should know',
      'Our values',
    ]) {
      expect(screen.getByRole('heading', { level: 2, name })).toBeInTheDocument();
    }
  });

  it('no longer has the story, mission or how-we-are-paid sections', () => {
    renderAbout();
    for (const name of ['Our story', 'Our mission', "How we're paid"]) {
      expect(screen.queryByRole('heading', { name })).toBeNull();
    }
    expect(document.body.textContent).not.toContain('TODO(owner)');
  });

  it('states that the business is an intermediary, not a lender', () => {
    renderAbout();
    expect(screen.getByText(/We are not a lender/)).toBeInTheDocument();
  });

  it('lists every loan type and links to consultation and contact', () => {
    renderAbout();
    for (const l of loans) {
      expect(screen.getByText(l.title, { selector: 'li' })).toBeInTheDocument();
    }
    expect(screen.getByRole('link', { name: 'Get a free consultation' })).toHaveAttribute(
      'href',
      '/consultation',
    );
    expect(screen.getByRole('link', { name: 'Contact us' })).toHaveAttribute('href', '/contact');
  });
});
