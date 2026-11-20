import { describe, expect, it } from 'vitest';
import config from '../tailwind.config';

describe('theme tokens', () => {
  it('defines the specified colours', () => {
    expect(config.theme?.extend?.colors).toEqual({
      primary: '#0F4C81',
      accent: '#F2A900',
      success: '#25D366',
      text: '#1F2937',
      muted: '#6B7280',
      background: '#FFFFFF',
      surface: '#F5F7FA',
    });
  });
});
