/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: '#0b5fff',
        ink: '#1f2937',
        mist: '#6b7280',
        panel: '#ffffff',
        line: '#d5d9df',
      },
      fontFamily: {
        sans: ['Manrope', 'Segoe UI', 'PingFang SC', 'sans-serif'],
      },
      boxShadow: {
        panel: '0 20px 45px rgba(15, 23, 42, 0.08)',
      },
    },
  },
  plugins: [],
}
