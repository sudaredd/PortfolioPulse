/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    darkMode: 'class',
    theme: {
        extend: {
            colors: {
                'terminal-bg': '#0a0e1a',
                'terminal-surface': '#111827',
                'terminal-border': '#1f2937',
                'terminal-accent': '#3b82f6',
                'terminal-success': '#10b981',
                'terminal-danger': '#ef4444',
                'terminal-warning': '#f59e0b',
            },
            fontFamily: {
                'mono': ['JetBrains Mono', 'Fira Code', 'monospace'],
            },
        },
    },
    plugins: [],
}
