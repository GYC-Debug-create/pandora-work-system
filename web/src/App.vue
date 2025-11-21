<template>
  <div class="app-shell">
    <el-config-provider :locale="zhCn">
      <router-view v-slot="{ Component }">
        <transition
          name="route-fade-slide"
          mode="out-in"
        >
          <component :is="Component" />
        </transition>
      </router-view>
    </el-config-provider>
  </div>
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

.app-shell {
  min-height: 100vh;
  width: 100vw;
  background:
    radial-gradient(circle at top left, rgba(102, 126, 234, 0.12), transparent 55%),
    radial-gradient(circle at bottom right, rgba(86, 204, 242, 0.1), transparent 55%),
    linear-gradient(145deg, var(--color-bg-base), var(--color-bg-elevated));
  display: flex;
  flex-direction: column;
}

.route-fade-slide-enter-active,
.route-fade-slide-leave-active {
  transition:
    opacity var(--transition-base),
    transform var(--transition-base);
}

.route-fade-slide-enter-from,
.route-fade-slide-leave-to {
  opacity: 0;
  transform: translateY(6px);
}
</style>
