import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import { resolve } from "path";
import purgecss from "@fullhuman/postcss-purgecss";

export default defineConfig(({ command }) => ({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": resolve(__dirname, "src"),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/styles/scss/variables" as *;\n@use "@/styles/scss/mixins" as *;\n`,
        api: "modern-compiler",
      },
    },
    ...(command === 'build' && {
      postcss: {
        plugins: [
          purgecss({
            content: ['./index.html', './src/**/*.{vue,js}'],
            safelist: {
              standard: [/^el-/, /^hljs/, /^v-enter/, /^v-leave/, /^fade-/],
              deep: [/el-/, /is-/],
            },
            defaultExtractor: (content) => content.match(/[\w-/:]+(?<!:)/g) || [],
            keyframes: true,
            fontFace: true,
            variables: true,
          }),
        ],
      },
    }),
  },
  build: {
    chunkSizeWarningLimit: 1500,
    sourcemap: false,
    rollupOptions: {
      maxParallelFileOps: 2,
      output: {
        manualChunks(id) {
          if (id.includes("node_modules")) {
            if (id.includes("echarts")) return "vendor-echarts";
            if (id.includes("element-plus")) return "vendor-element-plus";
            if (id.includes("vue") || id.includes("vue-router") || id.includes("pinia")) return "vendor-vue";
            return "vendor-misc";
          }
          return undefined;
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
      "/kkfileview": {
        target: "http://localhost:8012",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/kkfileview/, ""),
      },
    },
  },
}));
