import { useContext } from 'react';
import { Helmet } from 'react-helmet-async';
import { SeoActiveContext } from '../../seo/context';
import { BRAND, OG_IMAGE_PATH, absoluteUrl } from '../../seo/meta';
import type { JsonLd } from '../../seo/schema';

export interface SeoProps {
  title: string;
  description: string;
  /** Route path such as "/loans/gold-loan". Drives the canonical URL and og:url. */
  path: string;
  /** schema.org objects; null entries are ignored. */
  jsonLd?: (JsonLd | null)[];
  /** Keep this page out of search results (404). */
  noindex?: boolean;
}

/** Escape "<" so structured data can never close the script tag. */
const serialize = (data: JsonLd) => JSON.stringify(data).replace(/</g, '\\u003c');

export default function Seo({ title, description, path, jsonLd = [], noindex = false }: SeoProps) {
  const active = useContext(SeoActiveContext);
  if (!active) return null;
  const url = absoluteUrl(path);
  const image = absoluteUrl(OG_IMAGE_PATH);
  return (
    <Helmet>
      <title>{title}</title>
      <meta name="description" content={description} />
      {noindex ? <meta name="robots" content="noindex, follow" /> : <link rel="canonical" href={url} />}
      <meta property="og:type" content="website" />
      <meta property="og:site_name" content={BRAND} />
      <meta property="og:locale" content="en_IN" />
      <meta property="og:title" content={title} />
      <meta property="og:description" content={description} />
      <meta property="og:url" content={url} />
      <meta property="og:image" content={image} />
      <meta property="og:image:width" content="1200" />
      <meta property="og:image:height" content="630" />
      <meta property="og:image:alt" content={`${BRAND} – find the right loan, with expert help`} />
      <meta name="twitter:card" content="summary_large_image" />
      <meta name="twitter:title" content={title} />
      <meta name="twitter:description" content={description} />
      <meta name="twitter:image" content={image} />
      {jsonLd
        .filter((d): d is JsonLd => d !== null)
        .map((d, i) => (
          <script key={i} type="application/ld+json">
            {serialize(d)}
          </script>
        ))}
    </Helmet>
  );
}
