import { createApp } from 'vue'
import { createPinia } from 'pinia'
// Element Plus 按需引入 - 由 unplugin-vue-components 自动处理
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import { setLogStore } from './utils/request'
import { useLogStore } from './store/log'

import './assets/styles/main.css'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)

// 初始化 logStore 并设置到 request.js 中
const logStore = useLogStore()
setLogStore(logStore.addLog)

app.use(router)

app.mount('#app')
