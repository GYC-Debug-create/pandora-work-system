import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    host: '127.0.0.1',
    port: 3000,
    strictPort: true, // 如果端口被占用，则报错而不是尝试下一个端口
    hmr: {
      host: '127.0.0.1'
    },
    proxy: {
      // 配置代理，将API请求转发到后端服务
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        //rewrite: (path) => path.replace(/^\/api/, '')
      }
    },
    // 配置开发服务器处理单页应用的历史记录模式
    historyApiFallback: true
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    // 如果需要部署到SpringBoot项目中，可以将输出目录设置为SpringBoot的静态资源目录
    // outDir: '../backend/src/main/resources/static',
  }
})
