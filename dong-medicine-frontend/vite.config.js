import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import { resolve } from "path";

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": resolve(__dirname, "src"),
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes("node_modules")) {
            if (id.includes("echarts")) return "vendor-echarts";
            if (id.includes("element-plus")) return "vendor-element-plus";
            if (id.includes("vue") || id.includes("vue-router") || id.includes("pinia")) return "vendor-vue";
            return "vendor-misc";
          }
        },
      },
    },
  },
  server: {
    host: 'localhost',
    port: 5173,
    strictPort: false,
    open: true,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        rewrite: (path) => path,
      },
      "/images": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/videos": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/documents": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/public": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
