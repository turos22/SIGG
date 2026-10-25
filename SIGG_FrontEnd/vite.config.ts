import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import { defineConfig } from 'vite'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],
  resolve: {
    // Atalho @/ = src/ (igual ao @/ do Next). Também declarado em tsconfig.app.json, para o editor.
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
})
