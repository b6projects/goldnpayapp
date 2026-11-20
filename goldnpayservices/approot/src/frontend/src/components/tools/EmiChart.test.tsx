import { afterEach, describe, expect, it } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import EmiChart from './EmiChart';

afterEach(cleanup);

describe('EmiChart', () => {
  it('renders the doughnut and the legend labels with real recharts', () => {
    const { container } = render(<EmiChart principal={500000} interest={100000} />);
    expect(screen.getByTestId('emi-chart')).toBeInTheDocument();
    expect(container.querySelector('svg')).not.toBeNull();
    expect(screen.getByText('Principal')).toBeInTheDocument();
    expect(screen.getByText('Interest')).toBeInTheDocument();
  });
});
