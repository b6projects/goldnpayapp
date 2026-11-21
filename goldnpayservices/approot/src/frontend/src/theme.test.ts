import { describe, expect, it } from 'vitest';
import config from '../tailwind.config';

describe('theme tokens', () => {
  it('defines the specified colours', () => {
    expect(config.theme?.extend?.colors).toEqual({
      primary: '#28301C',
      secondary: '#323D25',
      accent: '#9A7E3B',
      sand: '#D2CF9A',
      success: '#25D366',
      text: '#28301C',
      muted: '#5E6552',
      background: '#FFFFFF',
      surface: '#EFEEDA',
      brand: { pdf: '#E2231A', excel: '#1D6F42' },
    });
  });
});
