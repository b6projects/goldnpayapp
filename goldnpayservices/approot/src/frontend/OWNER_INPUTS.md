# Owner inputs needed

Every placeholder that needs the owner's input is marked with the literal `TODO(owner)` in the source.
Find them all with: `grep -rn "TODO(owner)" src`. The M10 README folds this list in.

## src/config/site.ts
- (Done) Phone, WhatsApp and office address are set
- Map embed URL (`mapEmbedUrl`, map is hidden while empty)
- (Done) Grievance officer name, email, phone and hours (`grievanceOfficer`)
- (Done) Legal details for the Privacy, Terms and Disclaimer pages (`legal`): entity name, GSTIN, last-updated date, support email, official phones, pledged gold release charges
- Customer fee for loan facilitation (`legal.loanFeeStatement`): empty means the pages say nothing about a fee. Set one sentence once decided, e.g. "We do not charge you any fee to process a loan application."
- Social links (`social`, hidden while empty)
- Founding year (`foundedYear`)
- Flags: set `showSampleTestimonials` to false once real testimonials are in; `legalReviewed` is set to true, so the template banner is hidden on Privacy, Terms and Disclaimer

## src/pages
- Privacy, Terms and Disclaimer pages now carry the full GoldNPay text (DSA, old gold purchase, pledged gold release). The template banner is hidden (`legalReviewed: true`); a review by a legal professional is still recommended. Company details come from `site.legal` and `site.grievanceOfficer`.
- Before launch: check each DSA agreement for wording the lender requires on the website; add a cookie consent banner if analytics (Google Tag Manager) should load only after consent.
- Compliance note: the "explicit consent checkbox on every form" item stays pending until the real consultation form (GPS-9) ships.

## src/data
- `home.ts`: confirm the fee policy before keeping the "No-cost consultation" claim on the home page
- `partners.ts`: real partner list and permission to show names and logos (also replace the SVGs in public/images/partners)
- `stats.ts`: four real, verifiable figures
- `testimonials.ts`: real, consented testimonials

## SEO
- Set the production origin before deploying: either `url` in `src/config/site.ts` or the `VITE_SITE_URL` build argument (for example `VITE_SITE_URL=https://www.yourdomain.in ./build-frontend.sh`). It is baked into canonical links, Open Graph URLs, structured data, `sitemap.xml` and `robots.txt`. The build warns while it is still `example.com`.
- `serviceArea` (Tamil Nadu) drives the area in every page title ("Gold Loan in Tamil Nadu ...") and the `areaServed` markup. The office address stays in Chennai.
- Google Business Profile (done outside the code): create it with the same name, address and phone as `site.name`, `site.address` and `site.phone`, and add the website URL. Keep the three identical everywhere (site, profile, directories). The business name here ("GoldNPay Services") and the address line ("Gold N Pay, ...") differ today; pick one spelling.
- Add real `social` links when they exist: they flow into the homepage `sameAs` markup automatically.
- FAQ entries whose answer is still `TODO(owner)` are left out of the FAQ structured data until they are filled in.
