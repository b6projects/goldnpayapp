import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // react-helmet-async ships CommonJS that Node cannot import by name, so bundle it into the prerender build.
  ssr: { noExternal: ['react-helmet-async'] },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: false,
    coverage: {
      provider: 'v8',
      include: ['src/lib/**'],
      exclude: ['src/**/*.test.ts'],
      thresholds: { statements: 90, lines: 90, functions: 90, branches: 90 },
    },
  },
});
