import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const backendUrl = env.VITE_API_BASE
    ? env.VITE_API_BASE.replace('/api', '')
    : 'http://localhost:8080';

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: backendUrl,
          changeOrigin: true,
          secure: false,
        },
        '/ws': {
          target: backendUrl,
          ws: true,
          changeOrigin: true,
        },
      },
    },
    build: {
      outDir: 'dist',
      sourcemap: false,
    },
  };
});
