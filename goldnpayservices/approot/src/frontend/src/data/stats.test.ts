import { describe, expect, it } from 'vitest';
import { stats } from './stats';

describe('stats data', () => {
  it('has exactly 4 stats with label and value', () => {
    expect(stats).toHaveLength(4);
    stats.forEach((s) => {
      expect(s.label).toBeTruthy();
      expect(s.value).toBeTruthy();
    });
  });

  it('includes the spec example 20+ Lending partners', () => {
    expect(stats).toContainEqual({ label: 'Lending partners', value: '20+' });
  });
});
