import { afterEach, describe, expect, it } from 'vitest';
import { cleanup } from '@testing-library/react';
import { createRef } from 'react';
import { render, screen } from '@testing-library/react';
import Input from './Input';
import Select from './Select';
import Checkbox from './Checkbox';
afterEach(cleanup);


describe('form primitives', () => {
  it('Input associates its label and forwards the ref', () => {
    const ref = createRef<HTMLInputElement>();
    render(<Input ref={ref} label="Full name" />);
    const input = screen.getByLabelText('Full name');
    expect(ref.current).toBe(input);
    expect(input).not.toHaveAttribute('aria-invalid');
  });

  it('Input links the error via aria-describedby and aria-invalid', () => {
    render(<Input label="Phone" error="Enter a valid phone" />);
    const input = screen.getByLabelText('Phone');
    expect(input).toHaveAttribute('aria-invalid', 'true');
    expect(input).toHaveAccessibleDescription('Enter a valid phone');
  });

  it('Select associates label and error', () => {
    render(
      <Select label="Loan type" error="Pick one">
        <option value="a">A</option>
      </Select>,
    );
    const select = screen.getByLabelText('Loan type');
    expect(select).toHaveAttribute('aria-invalid', 'true');
    expect(select).toHaveAccessibleDescription('Pick one');
  });

  it('Select without error has no aria-invalid', () => {
    render(
      <Select label="City">
        <option value="a">A</option>
      </Select>,
    );
    expect(screen.getByLabelText('City')).not.toHaveAttribute('aria-invalid');
  });

  it('Checkbox associates label and error', () => {
    render(<Checkbox label="I agree" error="Required" />);
    const box = screen.getByLabelText('I agree');
    expect(box).toHaveAttribute('type', 'checkbox');
    expect(box).toHaveAttribute('aria-invalid', 'true');
    expect(box).toHaveAccessibleDescription('Required');
  });
});
