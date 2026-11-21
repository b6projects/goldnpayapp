import type { Config } from 'tailwindcss';

// Tailwind v3 is used because the spec lists a tailwind.config file.
// This is the only file where raw hex colour values may appear.
const colors = {
  primary: '#28301C',
  secondary: '#323D25',
  accent: '#9A7E3B',
  sand: '#D2CF9A',
  success: '#25D366',
  text: '#28301C',
  muted: '#5E6552',
  background: '#FFFFFF',
  surface: '#EFEEDA',
  brand: { pdf: '#E2231A', excel: '#1D6F42' },
};

const config: Config = {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors,
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        heading: ['Poppins', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
};

export default config;
