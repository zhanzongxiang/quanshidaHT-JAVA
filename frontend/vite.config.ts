import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiProxyTarget = env.VITE_API_PROXY_TARGET || 'http://localhost:8080'
  const devPort = Number(env.VITE_DEV_PORT || 5174)
  const hmrPort = Number(env.VITE_HMR_PORT || devPort)
  const hmrHost = env.VITE_HMR_HOST || undefined

  return {
    base: '/admin/',
    plugins: [vue()],
    server: {
      host: '0.0.0.0',
      port: devPort,
      strictPort: true,
      hmr: {
        host: hmrHost,
        port: hmrPort,
      },
      proxy: {
        '/api': {
          target: apiProxyTarget,
          changeOrigin: true,
        },
      },
    },
  }
})
