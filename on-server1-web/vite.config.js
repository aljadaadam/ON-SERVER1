import { defineConfig } from 'vite';
import path from 'path';

export default defineConfig({
  root: './src',
  publicDir: path.resolve(__dirname, 'public'),
  server: {
    port: 4000,
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: '../dist',
    emptyOutDir: true,
  },
});
