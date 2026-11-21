# Owner inputs needed

Every placeholder that needs the owner's input is marked with the literal `TODO(owner)` in the source.
Find them all with: `grep -rn "TODO(owner)" src`. The M10 README folds this list in.

## src/config/site.ts
- Business name (`name`, currently "GoldNPay Services")
- Production domain (`url`)
- (Done) Phone, WhatsApp and office address are set
- Map embed URL (`mapEmbedUrl`, map is hidden while empty)
- Cities served (`citiesServed`)
- Grievance officer name, email and phone (`grievanceOfficer`)
- Social links (`social`, hidden while empty)
- Founding year (`foundedYear`)
- Flags: set `showSampleTestimonials` to false once real testimonials are in; set `legalReviewed` to true after legal review of Privacy, Terms and Disclaimer

## src/pages
- `About.tsx`: founder story ("Our story") and the fee policy / how the business is paid ("How we're paid"). These live in the page, not in `src/data`.
- Privacy, Terms and Disclaimer pages are template text: have them reviewed by a legal professional, then set `legalReviewed` in `src/config/site.ts` to true to remove the template banner. The Privacy page shows the grievance officer from `site.grievanceOfficer`.
- Compliance note: the "explicit consent checkbox on every form" item stays pending until the real consultation form (GPS-9) ships.

## src/data
- `faqs.ts`: fee policy answer to "Do you charge customers a fee?" and how the business is paid
- `home.ts`: confirm the fee policy before keeping the "No-cost consultation" claim on the home page
- `partners.ts`: real partner list and permission to show names and logos (also replace the SVGs in public/images/partners)
- `stats.ts`: four real, verifiable figures
- `testimonials.ts`: real, consented testimonials
