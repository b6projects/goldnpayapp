import type { Config } from 'tailwindcss';

// Tailwind v3 is used because the spec lists a tailwind.config file.
// This is the only file where raw hex colour values may appear.
const colors = {
  primary: '#0F4C81',
  accent: '#F2A900',
  success: '#25D366',
  text: '#1F2937',
  muted: '#6B7280',
  background: '#FFFFFF',
  surface: '#F5F7FA',
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
