import { describe, expect, it } from 'vitest';
import { site } from './site';

describe('site config', () => {
  it('has the spec 5.1 string fields populated', () => {
    expect(site.name).toBeTruthy();
    expect(site.tagline).toBe('Find the right loan, with expert help.');
    expect(site.url).toBe('https://example.com');
    expect(site.phone).toBe('+91 87543 26019');
    expect(site.hours).toBe('Mon–Sat, 9:30 AM – 6:30 PM');
    expect(site.address).toContain('Chrompet, Chennai 600044');
    expect(site.mapEmbedUrl).toBe('');
    expect(site.foundedYear).toBe(2024);
  });

  it('has well-formed phoneE164 and whatsapp values', () => {
    expect(site.phoneE164).toMatch(/^\+\d{10,15}$/);
    expect(site.whatsapp).toMatch(/^\d+$/);
  });

  it('lists cities served and grievance officer', () => {
    expect(site.citiesServed).toEqual(['Chennai']);
    expect(site.grievanceOfficer.name).toBeTruthy();
    expect(site.grievanceOfficer.email).toContain('@');
    expect(site.grievanceOfficer.phone).toBeTruthy();
  });

  it('has social links present as empty strings', () => {
    expect(site.social).toEqual({ linkedin: '', instagram: '', facebook: '' });
  });

  it('has both boolean flags with the planned defaults', () => {
    expect(site.showSampleTestimonials).toBe(true);
    expect(site.legalReviewed).toBe(false);
  });
});
