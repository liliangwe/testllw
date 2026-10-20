import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// dev 环境将 /api 代理到本地 Spring Boot（8080）
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
