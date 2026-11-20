import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import Testimonials from './Testimonials';
import { testimonials } from '../../data/testimonials';

afterEach(cleanup);

describe('Testimonials', () => {
  it('shows the Sample label when the flag is true', () => {
    render(<Testimonials items={testimonials} showSample />);
    expect(screen.getByText('Sample')).toBeInTheDocument();
    expect(screen.getAllByRole('listitem')).toHaveLength(testimonials.length);
  });

  it('renders nothing when the flag is false and the list is empty', () => {
    const { container } = render(<Testimonials items={[]} showSample={false} />);
    expect(container).toBeEmptyDOMElement();
  });

  it('shows items without the Sample label when the flag is false', () => {
    render(<Testimonials items={testimonials} showSample={false} />);
    expect(screen.queryByText('Sample')).not.toBeInTheDocument();
    expect(screen.getAllByRole('listitem')).toHaveLength(testimonials.length);
  });
});
