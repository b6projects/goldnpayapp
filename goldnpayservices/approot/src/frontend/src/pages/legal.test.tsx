import { afterEach, describe, expect, it } from 'vitest';
import type { ReactElement } from 'react';
import { cleanup, render as rtlRender, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Privacy from './Privacy';
import Terms from './Terms';
import Disclaimer from './Disclaimer';
import LegalPage from '../components/legal/LegalPage';
import { site } from '../config/site';

afterEach(cleanup);

// The legal pages link to each other, so they need a router.
const render = (ui: ReactElement) => rtlRender(<MemoryRouter>{ui}</MemoryRouter>);

const banner = 'Template — to be reviewed by a legal professional before launch.';

describe('Privacy page', () => {
  it('covers DPDP rights, consent-based sharing and the grievance officer', () => {
    const { container } = render(<Privacy />);
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    const text = container.textContent ?? '';
    expect(text).toMatch(/DPDP Act 2023/);
    expect(text).toMatch(/withdraw your consent/);
    expect(text).toMatch(/request deletion/);
    expect(text).toMatch(/only with your consent/);
    expect(screen.getByRole('link', { name: site.grievanceOfficer.email })).toHaveAttribute(
      'href',
      `mailto:${site.grievanceOfficer.email}`,
    );
    expect(text).toContain(site.grievanceOfficer.name);
    expect(text).toContain(site.grievanceOfficer.phone);
  });
});

describe('Terms and Disclaimer pages', () => {
  it('Terms has one h1 and states not a lender and indicative', () => {
    const { container } = render(<Terms />);
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    expect(container.textContent).toMatch(/not a lender/);
    expect(container.textContent).toMatch(/indicative/);
  });

  it('Disclaimer has one h1 and states not a lender and indicative', () => {
    const { container } = render(<Disclaimer />);
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1);
    expect(container.textContent).toMatch(/not a lender/);
    expect(container.textContent).toMatch(/indicative/);
  });
});

describe('LegalPage banner', () => {
  it('shows the banner when requested', () => {
    render(<LegalPage title="X" showTemplateBanner>body</LegalPage>);
    expect(screen.getByRole('note')).toHaveTextContent(banner);
  });

  it('hides the banner when not requested', () => {
    render(<LegalPage title="X" showTemplateBanner={false}>body</LegalPage>);
    expect(screen.queryByRole('note')).not.toBeInTheDocument();
  });

  it('hides the banner by default once legalReviewed is true', () => {
    expect(site.legalReviewed).toBe(true);
    render(<LegalPage title="X">body</LegalPage>);
    expect(screen.queryByRole('note')).not.toBeInTheDocument();
  });
});
