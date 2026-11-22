import Hero from '../components/home/Hero';
import Stats from '../components/home/Stats';
import LoanGrid from '../components/home/LoanGrid';
import HowItWorks from '../components/home/HowItWorks';
import WhyUs from '../components/home/WhyUs';
import Partners from '../components/home/Partners';
import FaqPreview from '../components/home/FaqPreview';
import CtaBand from '../components/home/CtaBand';
import Seo from '../components/seo/Seo';
import { pageSeo } from '../seo/pageSeo';
import { faqSchema, organizationSchema, websiteSchema } from '../seo/schema';
import { faqs } from '../data/faqs';

// The homepage shows the first five FAQs (see FaqPreview), so only those go into the markup.
const homeJsonLd = [organizationSchema(), websiteSchema(), faqSchema(faqs.slice(0, 5))];

export default function Home() {
  return (
    <>
      <Seo {...pageSeo('/')} jsonLd={homeJsonLd} />
      <Hero />
      <Stats />
      <LoanGrid />
      <HowItWorks />
      <WhyUs />
      <Partners />
      {/* Customer testimonials are hidden until real, consented ones exist (see components/home/Testimonials.tsx). */}
      <FaqPreview />
      <CtaBand />
    </>
  );
}
