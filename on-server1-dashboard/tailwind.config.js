/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#FFFDF0',
          100: '#FFF8E1',
          200: '#FFECB3',
          300: '#FFE082',
          400: '#FFD54F',
          500: '#FFD700', // Main Yellow
          600: '#FFC107',
          700: '#FFB300',
          800: '#FFA000',
          900: '#FF8F00',
        },
        dark: {
          bg: '#0F0F23',
          surface: '#1A1A2E',
          card: '#16213E',
          border: '#374151',
        },
      },
    },
  },
  plugins: [],
};
