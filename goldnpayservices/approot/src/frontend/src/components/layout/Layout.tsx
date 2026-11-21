import { Suspense } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { useScrollToTop } from '../../hooks/useScrollToTop';
import SkipLink from './SkipLink';
import Header from './Header';
import Footer from './Footer';
import ScrollProgress from './ScrollProgress';
import WhatsAppFab from './WhatsAppFab';
import MobileStickyBar from './MobileStickyBar';

export default function Layout() {
  useScrollToTop();
  const { pathname } = useLocation();
  return (
    <div className="flex min-h-screen flex-col bg-background text-text">
      <SkipLink />
      <ScrollProgress />
      <Header />
      <main id="main" tabIndex={-1} className="flex-1 pb-20 focus:outline-none md:pb-0">
        <Suspense fallback={<p className="p-6 text-muted">Loading...</p>}>
          <div key={pathname} className="animate-fade-in">
            <Outlet />
          </div>
        </Suspense>
      </main>
      <Footer />
      <WhatsAppFab />
      <MobileStickyBar />
    </div>
  );
}
