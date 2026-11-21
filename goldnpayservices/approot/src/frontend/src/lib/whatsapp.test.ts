import { describe, expect, it } from 'vitest';
import { buildConsultationMessage, buildWhatsAppUrl } from './whatsapp';

describe('buildConsultationMessage', () => {
  it('includes all provided fields', () => {
    const msg = buildConsultationMessage({
      name: 'Asha',
      phone: '9876543210',
      loanType: 'Home Loan',
      amount: '₹10,00,000',
      tenure: '5 years',
      city: 'Chennai',
      message: 'Call me',
    });
    for (const s of [
      'Name: Asha',
      'Phone: 9876543210',
      'Loan type: Home Loan',
      'Amount: ₹10,00,000',
      'Tenure: 5 years',
      'City: Chennai',
      'Message: Call me',
    ]) {
      expect(msg).toContain(s);
    }
  });

  it('omits empty optional fields', () => {
    const msg = buildConsultationMessage({
      name: 'A',
      phone: '1',
      loanType: 'L',
      amount: '',
      city: '   ',
    });
    expect(msg).not.toContain('Amount');
    expect(msg).not.toContain('City');
    expect(msg).not.toContain('Tenure');
    expect(msg).not.toContain('Message');
  });
});

describe('buildWhatsAppUrl', () => {
  it('builds an encoded wa.me link with a digits-only number', () => {
    const text = 'Hi & bye?\n100% ₹';
    const url = buildWhatsAppUrl('+91 98765-43210', text);
    expect(url.startsWith('https://wa.me/919876543210?text=')).toBe(true);
    expect(decodeURIComponent(url.split('?text=')[1])).toBe(text);
    expect(url).not.toContain(' ');
    expect(url).not.toContain('&bye');
  });
});
