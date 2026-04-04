import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      // Windows 下 localhost 可能走 IPv6(::1)，与仅监听 IPv4 的后端不一致会触发 EACCES
      '/api': { target: 'http://127.0.0.1:8080', changeOrigin: true }
    }
  }
})
