import { defineConfig } from 'vite'
import path from 'node:path'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      // Path alias '@/' trỏ về src/ - convention chuẩn của shadcn/ui
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      // Proxy mọi request /api/* sang Spring Boot ở port 8080.
      // Frontend gọi axios.get('/api/v1/history') -> Vite forward sang http://localhost:8080/api/v1/history.
      // Nhờ vậy KHÔNG cần khai báo full URL trong code, dev không vướng CORS.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
