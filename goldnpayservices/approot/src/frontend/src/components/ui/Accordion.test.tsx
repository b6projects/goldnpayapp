import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Accordion from './Accordion';

const items = [
  { id: 'a', title: 'First', content: 'Alpha' },
  { id: 'b', title: 'Second', content: 'Beta' },
];
afterEach(cleanup);


describe('Accordion', () => {
  it('uses native details/summary, closed by default', () => {
    const { container } = render(<Accordion items={items} />);
    const details = container.querySelectorAll('details');
    expect(details).toHaveLength(2);
    details.forEach((d) => expect(d).not.toHaveAttribute('open'));
  });

  it('allows several items open at once', async () => {
    const user = userEvent.setup();
    const { container } = render(<Accordion items={items} />);
    await user.click(screen.getByText('First'));
    await user.click(screen.getByText('Second'));
    const details = container.querySelectorAll('details');
    expect(details[0]).toHaveAttribute('open');
    expect(details[1]).toHaveAttribute('open');
  });

  it('summaries are keyboard focusable', async () => {
    const user = userEvent.setup();
    render(<Accordion items={items} />);
    await user.tab();
    expect(screen.getByText('First').closest('summary')).toHaveFocus();
  });
});
