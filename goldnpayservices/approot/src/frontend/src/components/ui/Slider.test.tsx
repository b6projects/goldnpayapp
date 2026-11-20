import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup } from '@testing-library/react';
import { fireEvent, render, screen } from '@testing-library/react';
import Slider from './Slider';
afterEach(cleanup);


describe('Slider', () => {
  it('is a labelled native range with aria-valuetext', () => {
    render(<Slider label="Amount" min={0} max={10} value={5} onChange={() => {}} valueText="5 lakh" />);
    const s = screen.getByLabelText('Amount');
    expect(s).toHaveAttribute('type', 'range');
    expect(s).toHaveAttribute('aria-valuetext', '5 lakh');
  });

  it('falls back to the numeric value for aria-valuetext', () => {
    render(<Slider label="Tenure" min={0} max={10} value={7} onChange={() => {}} />);
    expect(screen.getByLabelText('Tenure')).toHaveAttribute('aria-valuetext', '7');
  });

  it('reports numeric changes', () => {
    const onChange = vi.fn();
    render(<Slider label="Rate" min={0} max={10} value={5} onChange={onChange} />);
    fireEvent.change(screen.getByLabelText('Rate'), { target: { value: '8' } });
    expect(onChange).toHaveBeenCalledWith(8);
  });
});
