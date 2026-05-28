/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['DM Sans', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
        display: ['Syne', 'sans-serif'],
      },
      colors: {
        surface: {
          0: '#080C14',
          1: '#0D1220',
          2: '#111827',
          3: '#1A2235',
          4: '#1F2A3D',
          5: '#243047',
        },
        accent: {
          blue:    '#3B82F6',
          cyan:    '#06B6D4',
          teal:    '#14B8A6',
          emerald: '#10B981',
        },
        severity: {
          critical: '#EF4444',
          high:     '#F97316',
          medium:   '#EAB308',
          low:      '#22C55E',
        },
        border: {
          dim:    'rgba(255,255,255,0.06)',
          subtle: 'rgba(255,255,255,0.10)',
          bright: 'rgba(255,255,255,0.18)',
        },
      },
      backgroundImage: {
        'grid-dark': "url(\"data:image/svg+xml,%3Csvg width='40' height='40' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M40 0H0v40' fill='none' stroke='rgba(255,255,255,0.03)' stroke-width='1'/%3E%3C/svg%3E\")",
        'glow-blue': 'radial-gradient(ellipse 60% 40% at 50% 0%, rgba(59,130,246,0.15) 0%, transparent 70%)',
      },
      animation: {
        'fade-in':     'fadeIn 0.4s ease forwards',
        'slide-up':    'slideUp 0.4s ease forwards',
        'pulse-slow':  'pulse 3s ease-in-out infinite',
        'shimmer':     'shimmer 1.6s linear infinite',
      },
      keyframes: {
        fadeIn:  { from: { opacity: 0 }, to: { opacity: 1 } },
        slideUp: { from: { opacity: 0, transform: 'translateY(12px)' }, to: { opacity: 1, transform: 'translateY(0)' } },
        shimmer: { from: { backgroundPosition: '-200% 0' }, to: { backgroundPosition: '200% 0' } },
      },
    },
  },
  plugins: [],
}
