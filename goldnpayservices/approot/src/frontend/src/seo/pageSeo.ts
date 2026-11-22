import { staticPageMeta } from './meta';

/** Props for <Seo> on a static page: its title, description and path from `staticPageMeta`. */
export function pageSeo(path: string) {
  const meta = staticPageMeta[path];
  if (!meta) throw new Error(`No SEO metadata defined for ${path}`);
  return { ...meta, path };
}
