import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// In sviluppo (npm run dev) le chiamate /api vengono inoltrate a Spring Boot.
// In produzione (npm run build) i file finiscono in src/main/resources/static.
export default defineConfig({
  plugins: [vue()],
  server: { proxy: { '/api': 'http://localhost:8080' } },
  build: { outDir: '../src/main/resources/static', emptyOutDir: true }
})
