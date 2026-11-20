import { afterEach, describe, expect, it, vi } from 'vitest';
import { act, cleanup, render, screen } from '@testing-library/react';
import { useDebouncedValue } from './useDebouncedValue';

afterEach(() => {
  cleanup();
  vi.useRealTimers();
});

function Probe({ value }: { value: number }) {
  const v = useDebouncedValue(value, 150);
  return <p data-testid="out">{v}</p>;
}

describe('useDebouncedValue', () => {
  it('returns the initial value immediately', () => {
    render(<Probe value={1} />);
    expect(screen.getByTestId('out')).toHaveTextContent('1');
  });

  it('updates only after the delay', () => {
    vi.useFakeTimers();
    const { rerender } = render(<Probe value={1} />);
    rerender(<Probe value={2} />);
    act(() => {
      vi.advanceTimersByTime(149);
    });
    expect(screen.getByTestId('out')).toHaveTextContent('1');
    act(() => {
      vi.advanceTimersByTime(1);
    });
    expect(screen.getByTestId('out')).toHaveTextContent('2');
  });

  it('restarts the timer on each change', () => {
    vi.useFakeTimers();
    const { rerender } = render(<Probe value={1} />);
    rerender(<Probe value={2} />);
    act(() => {
      vi.advanceTimersByTime(100);
    });
    rerender(<Probe value={3} />);
    act(() => {
      vi.advanceTimersByTime(100);
    });
    expect(screen.getByTestId('out')).toHaveTextContent('1');
    act(() => {
      vi.advanceTimersByTime(50);
    });
    expect(screen.getByTestId('out')).toHaveTextContent('3');
  });

  it('clears the pending timer on unmount', () => {
    vi.useFakeTimers();
    const { rerender, unmount } = render(<Probe value={1} />);
    rerender(<Probe value={2} />);
    unmount();
    expect(vi.getTimerCount()).toBe(0);
  });
});
