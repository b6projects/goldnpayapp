import { Cell, Legend, Pie, PieChart } from 'recharts';

export interface EmiChartProps {
  principal: number;
  interest: number;
}

const SIZE = 220;

function prefersReducedMotion(): boolean {
  return typeof window !== 'undefined' && typeof window.matchMedia === 'function'
    ? window.matchMedia('(prefers-reduced-motion: reduce)').matches
    : false;
}

function ChartLegend() {
  return (
    <ul className="mt-2 flex justify-center gap-4 text-sm text-text">
      <li className="flex items-center gap-2">
        <span aria-hidden="true" className="h-3 w-3 rounded-sm bg-primary" />
        Principal
      </li>
      <li className="flex items-center gap-2">
        <span aria-hidden="true" className="h-3 w-3 rounded-sm bg-accent" />
        Interest
      </li>
    </ul>
  );
}

/** Principal vs interest doughnut. The only module that imports recharts (lazy-loaded). */
export default function EmiChart({ principal, interest }: EmiChartProps) {
  const data = [
    { name: 'Principal', value: principal },
    { name: 'Interest', value: interest },
  ];
  return (
    <div data-testid="emi-chart" className="flex justify-center">
      <PieChart width={SIZE} height={SIZE + 36} accessibilityLayer={false}>
        <Pie
          data={data}
          dataKey="value"
          nameKey="name"
          innerRadius={60}
          outerRadius={95}
          stroke="none"
          isAnimationActive={!prefersReducedMotion()}
        >
          <Cell className="fill-primary" />
          <Cell className="fill-accent" />
        </Pie>
        <Legend content={<ChartLegend />} />
      </PieChart>
    </div>
  );
}
