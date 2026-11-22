import { createContext } from 'react';
import type { HelmetServerState } from 'react-helmet-async';

/** Filled by react-helmet-async while rendering on the server; read by the prerender step. */
export const helmetContext: { helmet?: HelmetServerState } = {};

/** True below <SeoProvider>. Lets <Seo> do nothing when a page is rendered on its own (unit tests). */
export const SeoActiveContext = createContext(false);
