import { Suspense } from 'react';
import { Outlet } from 'react-router-dom';
import { useScrollToTop } from '../../hooks/useScrollToTop';
import SkipLink from './SkipLink';
import Header from './Header';
import Footer from './Footer';
import WhatsAppFab from './WhatsAppFab';
import MobileStickyBar from './MobileStickyBar';

export default function Layout() {
  useScrollToTop();
  return (
    <div className="flex min-h-screen flex-col bg-background text-text">
      <SkipLink />
      <Header />
      <main id="main" tabIndex={-1} className="flex-1 pb-20 focus:outline-none md:pb-0">
        <Suspense fallback={<p className="p-6 text-muted">Loading...</p>}>
          <Outlet />
        </Suspense>
      </main>
      <Footer />
      <WhatsAppFab />
      <MobileStickyBar />
    </div>
  );
}
