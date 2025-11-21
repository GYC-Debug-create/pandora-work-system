import { createApp } from 'vue'
import { createPinia } from 'pinia'

// Google Fonts - Inter
import '@fontsource/inter/400.css'
import '@fontsource/inter/500.css'
import '@fontsource/inter/600.css'
import '@fontsource/inter/700.css'

// Design Tokens (must load before Element Plus)
import './styles/design-tokens.css'

// Element Plus 按需引入 - 由 unplugin-vue-components 自动处理
import 'element-plus/dist/index.css'
// Element Plus 暗色主题
import 'element-plus/theme-chalk/dark/css-vars.css'

import App from './App.vue'
import router from './router'
import { setLogStore } from './utils/request'
import { useLogStore } from './store/log'

import './assets/styles/main.css'
import './assets/styles/theme.css'
import './styles/element-overrides.css'

// 导入所有 Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 全局注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)

// 初始化 logStore 并设置到 request.js 中
const logStore = useLogStore()
setLogStore(logStore.addLog)

app.use(router)

app.mount('#app')
