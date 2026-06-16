import react from '@vitejs/plugin-react';
import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => ({
  plugins: [react()],
  build: {
    minify: mode !== 'development',
  },
  // https://github.com/vitejs/vite/issues/12423#issuecomment-2080351394
  optimizeDeps: {
    include: ['@mui/material/Tooltip'],
  },
  test: {
    environment: 'jsdom',
    coverage: {
      reporter: ['text', 'html'],
    },
  },
  //We define the process.env to avoid 'Uncaught ReferenceError: process is not defined'.
  //Dependencies (such as react-trello) might expect environment variables to be defined (REDUX_LOGGING in this case).
  define: {
    'process.env': { ...process.env, ...loadEnv(mode, process.cwd()) },
  },
}));
