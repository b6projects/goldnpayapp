# Owner inputs needed

Every placeholder that needs the owner's input is marked with the literal `TODO(owner)` in the source.
Find them all with: `grep -rn "TODO(owner)" src`. The M10 README folds this list in.

## src/config/site.ts
- Business name (`name`, currently "GoldNPay Services")
- Production domain (`url`)
- Phone, display format (`phone`) and `phoneE164`
- WhatsApp number (`whatsapp`)
- Email (`email`)
- Office address (`address`)
- Map embed URL (`mapEmbedUrl`, map is hidden while empty)
- Cities served (`citiesServed`)
- Grievance officer name, email and phone (`grievanceOfficer`)
- Social links (`social`, hidden while empty)
- Founding year (`foundedYear`)
- Flags: set `showSampleTestimonials` to false once real testimonials are in; set `legalReviewed` to true after legal review of Privacy, Terms and Disclaimer

## src/data
- `faqs.ts`: fee policy answer to "Do you charge customers a fee?" and how the business is paid
- `home.ts`: confirm the fee policy before keeping the "No-cost consultation" claim on the home page
- `partners.ts`: real partner list and permission to show names and logos (also replace the SVGs in public/images/partners)
- `stats.ts`: four real, verifiable figures
- `testimonials.ts`: real, consented testimonials
