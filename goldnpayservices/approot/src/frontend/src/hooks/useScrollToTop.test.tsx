import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { Link, MemoryRouter } from 'react-router-dom';
import { useScrollToTop } from './useScrollToTop';

function Probe() {
  useScrollToTop();
  return <Link to="/about">go</Link>;
}

describe('useScrollToTop', () => {
  it('scrolls to top on navigation', () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <Probe />
      </MemoryRouter>,
    );
    vi.mocked(window.scrollTo).mockClear();
    fireEvent.click(screen.getByText('go'));
    expect(window.scrollTo).toHaveBeenCalledWith(0, 0);
  });
});
