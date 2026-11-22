import { Outlet } from 'react-router-dom';
import { HelmetProvider } from 'react-helmet-async';
import { SeoActiveContext, helmetContext } from './context';

/** Route element that wraps every page so <Seo> can set the document head. */
export default function SeoProvider() {
  return (
    <HelmetProvider context={helmetContext}>
      <SeoActiveContext.Provider value>
        <Outlet />
      </SeoActiveContext.Provider>
    </HelmetProvider>
  );
}
