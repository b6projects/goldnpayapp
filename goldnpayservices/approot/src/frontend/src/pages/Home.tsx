import Hero from '../components/home/Hero';
import Stats from '../components/home/Stats';
import LoanGrid from '../components/home/LoanGrid';
import HowItWorks from '../components/home/HowItWorks';
import WhyUs from '../components/home/WhyUs';
import Partners from '../components/home/Partners';
import Testimonials from '../components/home/Testimonials';
import FaqPreview from '../components/home/FaqPreview';
import CtaBand from '../components/home/CtaBand';

export default function Home() {
  return (
    <>
      <Hero />
      <Stats />
      <LoanGrid />
      <HowItWorks />
      <WhyUs />
      <Partners />
      <Testimonials />
      <FaqPreview />
      <CtaBand />
    </>
  );
}
