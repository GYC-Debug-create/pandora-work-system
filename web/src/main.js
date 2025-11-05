import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { setLogStore } from './utils/request'
import { useLogStore } from './store/log'

import './assets/styles/main.css'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)

// 初始化 logStore 并设置到 request.js 中
const logStore = useLogStore()
setLogStore(logStore.addLog)

app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

app.mount('#app')
