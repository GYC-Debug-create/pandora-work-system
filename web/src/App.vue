<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { watch, onMounted } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()

/**
 * 同步主题类到 html 元素
 */
const syncThemeClass = (theme) => {
  const html = document.documentElement
  if (theme === 'dark') {
    html.classList.add('dark')
  } else {
    html.classList.remove('dark')
  }
}

// 初始化时同步主题
onMounted(() => {
  syncThemeClass(themeStore.theme)
})

// 监听主题变化
watch(
  () => themeStore.theme,
  (newTheme) => {
    syncThemeClass(newTheme)
  }
)
</script>

<style>
#app {
  height: 100vh;
  width: 100vw;
}
</style>
