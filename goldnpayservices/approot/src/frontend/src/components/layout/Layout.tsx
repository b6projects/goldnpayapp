import { Suspense } from 'react';
import { Outlet } from 'react-router-dom';
import { useScrollToTop } from '../../hooks/useScrollToTop';

export default function Layout() {
  useScrollToTop();
  return (
    <main id="main" className="min-h-screen bg-background text-text">
      <Suspense fallback={<p className="p-6 text-muted">Loading...</p>}>
        <Outlet />
      </Suspense>
    </main>
  );
}
