/* eslint-disable react-refresh/only-export-components -- server entry, not a hot-reloaded module */
import { StrictMode } from 'react';
import { renderToPipeableStream } from 'react-dom/server';
import { StaticRouter, useRoutes } from 'react-router-dom';
import { routes } from './routes';
import { helmetContext } from './seo/context';
import { SITE_URL, indexablePaths } from './seo/meta';

/*
 * Server entry used only by scripts/prerender.mjs at build time (vite build --ssr).
 * It renders one URL to HTML, waiting for lazy route chunks so the output is complete.
 */

function AppRoutes() {
  return useRoutes(routes);
}

type Sink = Parameters<ReturnType<typeof renderToPipeableStream>['pipe']>[0];

/** Renders `url` into `sink` (a Node Writable) and resolves with the head tags React Helmet collected. */
export function renderApp(url: string, sink: Sink): Promise<NonNullable<typeof helmetContext.helmet>> {
  helmetContext.helmet = undefined;
  return new Promise((resolve, reject) => {
    const stream = renderToPipeableStream(
      <StrictMode>
        <StaticRouter location={url}>
          <AppRoutes />
        </StaticRouter>
      </StrictMode>,
      {
        onAllReady() {
          stream.pipe(sink);
          const helmet = helmetContext.helmet;
          if (helmet) resolve(helmet);
          else reject(new Error(`No <Seo> rendered for ${url}`));
        },
        onShellError: reject,
        onError: reject,
      },
    );
  });
}

export { SITE_URL, indexablePaths };
