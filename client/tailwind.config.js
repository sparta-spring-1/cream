/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: "var(--color-primary)",
                "bg-light": "var(--color-bg-light)",
                "bg-dark": "var(--color-bg-dark)",
            },
            fontFamily: {
                sans: ["var(--font-main)", "sans-serif"],
            }
        },
    },
    plugins: [],
}
