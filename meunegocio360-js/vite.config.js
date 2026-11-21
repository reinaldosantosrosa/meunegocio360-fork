import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
<<<<<<< HEAD
      "/api": {
        target: "http://localhost:8080",
=======
      '/api': {
        target: 'http://localhost:8080',
>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
        changeOrigin: true,
      },
    },
  },
<<<<<<< HEAD
});
=======
})
>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
