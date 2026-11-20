import { describe, expect, it } from 'vitest';
import { trackEvent } from './analytics';

describe('trackEvent', () => {
  it('is a no-op and does not throw', () => {
    expect(() => trackEvent('test_event', { a: 1 })).not.toThrow();
    expect(trackEvent('test_event')).toBeUndefined();
  });
});
